import {Injectable, OnDestroy} from "@angular/core";
import {catchError, EMPTY, Observable, Subject, takeUntil} from "rxjs";
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

@Injectable({
  providedIn: "root",
})
export class WebSocketService implements OnDestroy {
  private socket$: Option<Subject<WebSocketMessage>> = Option.none();
  private readonly messages$: Subject<WebSocketMessage> = new Subject<WebSocketMessage>();
  private readonly destroy$: Subject<void> = new Subject<void>();

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
    console.log("Connecting to WebSocket...");

    if (this.socket$.isSome()) {
      return;
    }

    const socket: WebSocketSubject<WebSocketMessage> = webSocket({
      url: WS_ENDPOINT,
      openObserver: {
        next: () => console.log("WebSocket connection established"),
      },
      closeObserver: {
        next: () => {
          console.warn(
            "WebSocket connection closed - attempting to reconnect...",
          );
          this.socket$ = Option.none();
          this.connect();
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
      .subscribe((msg) => this.messages$.next(msg));
  }
}
