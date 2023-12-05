import { Injectable, OnDestroy } from '@angular/core';
import {
  catchError,
  delay,
  EMPTY,
  filter,
  map,
  Observable,
  of,
  Subject,
  Subscription,
  takeUntil,
  timer,
} from 'rxjs';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Option } from '../util';

const WS_ENDPOINT: string = 'ws://localhost:7070/ws'; // TODO determine protocol and host based on environment

enum WebSocketMessageMethod {
  HEARTBEAT = 'HEARTBEAT',
  EVENT = 'EVENT',
}

interface HeartbeatMessage {}

/*
    EventTopicDTO topic;

    String eventName;

    long eventVersion;

    Object payload;
 */

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

interface WebSocketMessage {
  method: WebSocketMessageMethod;
  heartbeat?: HeartbeatMessage;
  event?: EventMessage;
}

const HEARTBEAT_INTERVAL_MS: number = 5000;
const HEARTBEAT_TIMEOUT_MS: number = 10000;

@Injectable({
  providedIn: 'root',
})
export class WebSocketService implements OnDestroy {
  private socket$: Option<Subject<WebSocketMessage>> = Option.none();
  private readonly messages$: Subject<WebSocketMessage> =
    new Subject<WebSocketMessage>();
  private readonly destroy$: Subject<void> = new Subject<void>();
  private reconnectionFailures: number = 0;
  private heartbeatSub: Option<Subscription> = Option.none();
  private heartbeatTimeoutSub: Option<Subscription> = Option.none();

  constructor() {
    this.connect();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();

    this.socket$.ifSome((socket) => socket.complete());
  }

  // TODO Replace method with new subscribeTo(EventTopic)
  getEvents$(): Observable<EventMessage> {
    return this.messages$.asObservable().pipe(
      filter((msg) => msg.method === WebSocketMessageMethod.EVENT),
      map((msg) => msg.event!),
    );
  }

  private send(msg: WebSocketMessage) {
    this.socket$.ifSome((socket) => socket.next(msg));
  }

  private connect() {
    if (this.socket$.isSome()) {
      return;
    }

    console.log(
      `Connecting to WebSocket... Attempt ${this.reconnectionFailures + 1}`,
    );

    const socket: WebSocketSubject<WebSocketMessage> = webSocket({
      url: WS_ENDPOINT,
      openObserver: {
        next: () => {
          this.reconnectionFailures = 0;
          this.startHeartbeat();
        },
      },
      closeObserver: {
        next: (closeEvent) => {
          console.warn(
            `WebSocket connection closed with code ${closeEvent.code} and reason '${closeEvent.reason}' - attempting to reconnect...`,
          );
          this.stopHeartbeat();
          this.socket$ = Option.none();
          const backoff = Math.pow(2, this.reconnectionFailures) * 1000;
          this.reconnectionFailures++;
          of(1)
            .pipe(delay(backoff))
            .subscribe(() => this.connect());
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
