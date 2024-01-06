import { Injectable, OnDestroy } from '@angular/core';
import {
  catchError,
  delay,
  EMPTY,
  filter,
  finalize,
  map,
  Observable,
  of,
  startWith,
  Subject,
  Subscription,
  switchMap,
  take,
  takeUntil,
  tap,
  timer,
} from 'rxjs';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Option } from '../util';
import { environment } from '../../../../environments';
import { LoginService } from '../../login';

enum WebSocketMessageMethod {
  HEARTBEAT = 'HEARTBEAT',
  EVENT = 'EVENT',
  PERMISSION_EVENT = 'PERMISSION_EVENT',
  SUBSCRIBE = 'SUBSCRIBE',
  SUBSCRIBED = 'SUBSCRIBED',
  SUBSCRIBE_TO_PERMISSIONS = 'SUBSCRIBE_TO_PERMISSIONS',
  SUBSCRIBED_TO_PERMISSIONS = 'SUBSCRIBED_TO_PERMISSIONS',
  UNSUBSCRIBE = 'UNSUBSCRIBE',
  UNSUBSCRIBED = 'UNSUBSCRIBED',
  UNSUBSCRIBE_FROM_PERMISSIONS = 'UNSUBSCRIBE_FROM_PERMISSIONS',
  UNSUBSCRIBED_FROM_PERMISSIONS = 'UNSUBSCRIBED_FROM_PERMISSIONS',
}

interface HeartbeatMessage {}

interface SubscribeMessage {
  aggregateType: string;
  aggregateId: string;
  eventName?: string;
}

interface SubscribedMessage {
  aggregateType: string;
  aggregateId: string;
  eventName?: string;
}

interface SubscribeToPermissionsMessage {
  aggregateType: string;
  aggregateId?: string;
  action?: string;
}

interface SubscribedToPermissionsMessage {
  aggregateType: string;
  aggregateId?: string;
  action?: string;
}

interface UnsubscribeMessage {
  aggregateType: string;
  aggregateId: string;
  eventName?: string;
}

interface UnsubscribedMessage {
  aggregateType: string;
  aggregateId: string;
  eventName?: string;
}

interface UnsubscribeFromPermissionsMessage {
  aggregateType: string;
  aggregateId?: string;
  action?: string;
}

interface UnsubscribedFromPermissionsMessage {
  aggregateType: string;
  aggregateId?: string;
  action?: string;
}

// TODO Should not export since its a DTO
export interface EventTopicDTO {
  aggregateType: string;
  aggregateId: string;
  version: number;
}

export interface EventMessage {
  topic: EventTopicDTO;
  eventName: string;
  eventVersion: number;
  payload: any;
}

export enum PermissionEventType {
  ADDED = 'ADDED',
  REMOVED = 'REMOVED',
}

export interface PermissionEventMessage {
  type: PermissionEventType;
  aggregateType: string;
  aggregateId?: string;
  action: string;
  userId: string;
}

interface WebSocketMessage {
  method: WebSocketMessageMethod;
  token?: string;
  heartbeat?: HeartbeatMessage;
  event?: EventMessage;
  permissionEvent?: PermissionEventMessage;
  subscribe?: SubscribeMessage;
  subscribed?: SubscribedMessage;
  subscribeToPermissions?: SubscribeToPermissionsMessage;
  subscribedToPermissions?: SubscribedToPermissionsMessage;
  unsubscribe?: UnsubscribeMessage;
  unsubscribed?: UnsubscribedMessage;
  unsubscribeFromPermissions?: UnsubscribeFromPermissionsMessage;
  unsubscribedFromPermissions?: UnsubscribedFromPermissionsMessage;
}

const HEARTBEAT_INTERVAL_MS: number = 5000;
const HEARTBEAT_TIMEOUT_MS: number = 10000;

@Injectable({
  providedIn: 'root',
})
export class WebSocketService implements OnDestroy {
  private socket$: Option<Subject<WebSocketMessage>> = Option.none();
  private reconnect$: Subject<void> = new Subject<void>();
  private readonly messages$: Subject<WebSocketMessage> =
    new Subject<WebSocketMessage>();
  private readonly destroy$: Subject<void> = new Subject<void>();
  private reconnectionFailures: number = 0;
  private heartbeatSub: Option<Subscription> = Option.none();
  private heartbeatTimeoutSub: Option<Subscription> = Option.none();
  private isConnected: boolean = false;
  private token: Option<string> = Option.none();

  constructor(private readonly loginService: LoginService) {
    this.loginService
      .getToken()
      .pipe(
        map((token) => token.map((t) => t.getValue())),
        tap((token) => (this.token = token)),
        tap((token) => {
          if (token.isNone()) {
            this.disconnect();
          } else {
            this.connect();
          }
        }),
        takeUntil(this.destroy$),
      )
      .subscribe();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();

    this.socket$.ifSome((socket) => socket.complete());
  }

  subscribeTo(props: {
    aggregateType: string;
    aggregateId: string;
    eventName?: string;
  }): Observable<EventMessage> {
    const { aggregateType, aggregateId, eventName } = props;

    const subscribeMsg: SubscribeMessage = {
      aggregateType,
      aggregateId,
      eventName,
    };
    const msg: WebSocketMessage = {
      method: WebSocketMessageMethod.SUBSCRIBE,
      token: this.token.orElse(''),
      subscribe: subscribeMsg,
    };

    this.send(msg);

    return this.getEventsAfter$((msg) =>
      Option.someOrNone(msg.subscribed)
        .map(
          (subscribed) =>
            subscribed.aggregateType === aggregateType &&
            subscribed.aggregateId === aggregateId &&
            Option.someOrNone(subscribed.eventName).equals(
              Option.someOrNone(eventName),
            ),
        )
        .orElse(false),
    ).pipe(
      filter(
        (event) =>
          event.topic.aggregateType === aggregateType &&
          event.topic.aggregateId === aggregateId &&
          Option.someOrNone(eventName)
            .map((name) => name == event.eventName)
            .orElse(true),
      ),
      finalize(() =>
        this.unsubscribe({ aggregateType, aggregateId, eventName }),
      ),
    );
  }

  subscribeToPermissions(props: {
    aggregateType: string;
    aggregateId?: string;
    action?: string;
  }): Observable<PermissionEventMessage> {
    const { aggregateType, aggregateId, action } = props;

    const subscribeToPermissionsMsg: SubscribeToPermissionsMessage = {
      aggregateType,
      aggregateId,
      action,
    };
    const msg: WebSocketMessage = {
      method: WebSocketMessageMethod.SUBSCRIBE_TO_PERMISSIONS,
      token: this.token.orElse(''),
      subscribeToPermissions: subscribeToPermissionsMsg,
    };

    this.send(msg);

    return this.getPermissionEventsAfter$((msg) =>
      Option.someOrNone(msg.subscribedToPermissions)
        .map(
          (subscribed) =>
            subscribed.aggregateType === aggregateType &&
            Option.someOrNone(subscribed.aggregateId).equals(
              Option.someOrNone(aggregateId),
            ) &&
            Option.someOrNone(subscribed.action).equals(
              Option.someOrNone(action),
            ),
        )
        .orElse(false),
    ).pipe(
      filter(
        (event) =>
          event.aggregateType === aggregateType &&
          (aggregateId === undefined || event.aggregateId === aggregateId),
      ),
      finalize(() =>
        this.unsubscribeFromPermissions({ aggregateType, aggregateId, action }),
      ),
    );
  }

  private disconnect(): void {
    this.socket$.ifSome((socket) => socket.complete());
    this.socket$ = Option.none();
    this.isConnected = false;
    this.reconnectionFailures = 0;
  }

  /**
   * Events are sent immediately after (re)connecting to the WebSocket.
   * If the socket is already connected an initial event is sent immediately.
   */
  onConnected$(): Observable<any> {
    const result$ = this.reconnect$.asObservable();

    if (this.isConnected) {
      return result$.pipe(startWith(null)); // Send initial event immediately
    }

    return result$;
  }

  private unsubscribe(props: {
    aggregateType: string;
    aggregateId: string;
    eventName?: string;
  }) {
    const { aggregateType, aggregateId, eventName } = props;

    const unsubscribeMsg: UnsubscribeMessage = {
      aggregateType,
      aggregateId,
      eventName,
    };
    const msg: WebSocketMessage = {
      method: WebSocketMessageMethod.UNSUBSCRIBE,
      token: this.token.orElse(''),
      unsubscribe: unsubscribeMsg,
    };

    this.send(msg);
  }

  private unsubscribeFromPermissions(props: {
    aggregateType: string;
    aggregateId?: string;
    action?: string;
  }) {
    const { aggregateType, aggregateId, action } = props;

    const unsubscribeFromPermissionsMsg: UnsubscribeFromPermissionsMessage = {
      aggregateType,
      aggregateId,
      action,
    };
    const msg: WebSocketMessage = {
      method: WebSocketMessageMethod.UNSUBSCRIBE_FROM_PERMISSIONS,
      token: this.token.orElse(''),
      unsubscribeFromPermissions: unsubscribeFromPermissionsMsg,
    };

    this.send(msg);
  }

  private getEventsAfter$(
    afterSelector: (msg: WebSocketMessage) => boolean,
  ): Observable<EventMessage> {
    const after$ = this.messages$
      .asObservable()
      .pipe(filter(afterSelector), take(1));

    const events$ = this.messages$.asObservable().pipe(
      filter((msg) => msg.method === WebSocketMessageMethod.EVENT),
      map((msg) => msg.event!),
    );

    return after$.pipe(switchMap(() => events$));
  }

  private getPermissionEventsAfter$(
    afterSelector: (msg: WebSocketMessage) => boolean,
  ): Observable<PermissionEventMessage> {
    const after$ = this.messages$
      .asObservable()
      .pipe(filter(afterSelector), take(1));

    const events$ = this.messages$.asObservable().pipe(
      filter((msg) => msg.method === WebSocketMessageMethod.PERMISSION_EVENT),
      map((msg) => msg.permissionEvent!),
    );

    return after$.pipe(switchMap(() => events$));
  }

  private send(msg: WebSocketMessage) {
    this.socket$.ifSome((socket) => socket.next(msg));
  }

  private connect() {
    if (this.socket$.isSome() && this.token.isNone()) {
      return;
    }

    console.log(`Connecting to WebSocket...`);

    const socket: WebSocketSubject<WebSocketMessage> = webSocket({
      url: environment.webSocketUrl,
      openObserver: {
        next: () => {
          this.reconnectionFailures = 0;
          this.startHeartbeat();
          this.reconnect$.next();
          this.isConnected = true;
        },
      },
      closeObserver: {
        next: (closeEvent) => {
          console.warn(
            `WebSocket connection closed with code ${closeEvent.code} and reason '${closeEvent.reason}' - attempting to reconnect...`,
          );
          const isUnauthorized = closeEvent.code === 4001;
          this.stopHeartbeat();
          this.socket$ = Option.none();
          this.isConnected = false;

          if (isUnauthorized) {
            this.loginService.logout();
          } else if (this.token.isSome()) {
            const backoff = Math.pow(2, this.reconnectionFailures) * 1000;
            this.reconnectionFailures++;
            of(1)
              .pipe(delay(backoff), takeUntil(this.destroy$))
              .subscribe(() => this.connect());
          }
        },
      },
    });
    this.socket$ = Option.some(socket);

    socket
      .pipe(
        catchError((e) => {
          console.error('WebSocket connection error:', e);
          return EMPTY;
        }),
        takeUntil(this.destroy$),
      )
      .subscribe((msg) => this.onMessage(msg));
  }

  private onMessage(msg: WebSocketMessage) {
    if (msg.method === WebSocketMessageMethod.HEARTBEAT) {
      this.stopHeartbeatTimeout();
    } else {
      this.messages$.next(msg);
    }
  }

  private startHeartbeat() {
    this.heartbeatTimeoutSub = Option.none();
    this.heartbeatSub = Option.some(
      timer(HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS)
        .pipe(
          takeUntil(this.destroy$),
          filter(() => this.lastHeartbeatReceived()),
        )
        .subscribe(() => this.sendHeartbeat()),
    );
  }

  private lastHeartbeatReceived(): boolean {
    return this.heartbeatTimeoutSub.isNone();
  }

  private sendHeartbeat() {
    const msg: WebSocketMessage = {
      method: WebSocketMessageMethod.HEARTBEAT,
      token: this.token.orElse(''),
      heartbeat: {},
    };

    this.send(msg);

    this.startHeartbeatTimeout();
  }

  private stopHeartbeat() {
    this.heartbeatSub.ifSome((sub) => sub.unsubscribe());
    this.heartbeatSub = Option.none();
  }

  private startHeartbeatTimeout() {
    this.heartbeatTimeoutSub.ifSome((sub) => sub.unsubscribe());
    this.heartbeatTimeoutSub = Option.some(
      timer(HEARTBEAT_TIMEOUT_MS)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          console.error(
            'No heartbeat received in time - closing WebSocket connection',
          );
          this.socket$.ifSome((socket) => socket.complete());
        }),
    );
  }

  private stopHeartbeatTimeout() {
    this.heartbeatTimeoutSub.ifSome((sub) => sub.unsubscribe());
    this.heartbeatTimeoutSub = Option.none();
  }
}
