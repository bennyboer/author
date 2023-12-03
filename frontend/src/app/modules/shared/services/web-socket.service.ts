import {Injectable, OnDestroy} from "@angular/core";
import {catchError, delay, EMPTY, filter, Observable, of, Subject, Subscription, takeUntil, timer} from "rxjs";
import {webSocket, WebSocketSubject} from "rxjs/webSocket";
import {Option} from "../util";

const WS_ENDPOINT: string = "ws://localhost:7070/ws"; // TODO determine protocol and host based on environment

enum WebSocketMessageMethod {
  HEARTBEAT = "HEARTBEAT",
  SUBSCRIBE = "SUBSCRIBE",
  UNSUBSCRIBE = "UNSUBSCRIBE",
  DISPATCH_COMMAND = "DISPATCH_COMMAND",
  EVENT = "EVENT"
}

interface HeartbeatMessage {
}

interface SubscribeMessage {
  test: string;
}

interface UnsubscribeMessage {

}

interface DispatchCommandMessage {

}

interface EventMessage {

}

interface WebSocketMessage {
  method: WebSocketMessageMethod;
  heartbeat?: HeartbeatMessage;
  subscribe?: SubscribeMessage;
  unsubscribe?: UnsubscribeMessage;
  dispatchCommand?: DispatchCommandMessage;
  event?: EventMessage;
}

const HEARTBEAT_INTERVAL_MS: number = 5000;
const HEARTBEAT_TIMEOUT_MS: number = 10000;

@Injectable({
  providedIn: "root",
})
export class WebSocketService implements OnDestroy {
  private socket$: Option<Subject<WebSocketMessage>> = Option.none();
  private readonly messages$: Subject<WebSocketMessage> = new Subject<WebSocketMessage>();
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

  getMessages$(): Observable<WebSocketMessage> {
    return this.messages$.asObservable();
  }

  subscribe(topic: string) {
    const subscribeMessage: SubscribeMessage = {
      test: "Hello World",
    };
    const msg: WebSocketMessage = {
      method: WebSocketMessageMethod.SUBSCRIBE,
      subscribe: subscribeMessage,
    };

    this.send(msg);
  }

  private send(msg: WebSocketMessage) {
    this.socket$.ifSome((socket) => socket.next(msg));
  }

  private connect() {
    if (this.socket$.isSome()) {
      return;
    }

    console.log(`Connecting to WebSocket... Attempt ${this.reconnectionFailures + 1}`);

    const socket: WebSocketSubject<WebSocketMessage> = webSocket({
      url: WS_ENDPOINT,
      openObserver: {
        next: () => {
          console.log("WebSocket connection established");
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
          of(1).pipe(delay(backoff)).subscribe(() => this.connect());
        },
      },
    });
    this.socket$ = Option.some(socket);

    socket
      .pipe(
        catchError((e) => {
          console.error("WebSocket connection error:", e);
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
    this.heartbeatSub = Option.some(timer(HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS)
      .pipe(takeUntil(this.destroy$), filter(() => this.lastHeartbeatReceived()))
      .subscribe(() => this.sendHeartbeat()));
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
    this.heartbeatTimeoutSub = Option.some(timer(HEARTBEAT_TIMEOUT_MS)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        console.error("No heartbeat received in time - closing WebSocket connection");
        this.socket$.ifSome((socket) => socket.complete());
      }));
  }

  private stopHeartbeatTimeout() {
    this.heartbeatTimeoutSub.ifSome((sub) => sub.unsubscribe());
    this.heartbeatTimeoutSub = Option.none();
  }
}
