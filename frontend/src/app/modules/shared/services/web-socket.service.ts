import { Injectable, OnDestroy } from '@angular/core';
import { catchError, EMPTY, Observable, Subject, takeUntil } from 'rxjs';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Option } from '../util';

const WS_ENDPOINT: string = 'ws://localhost:8000/ws'; // TODO determine protocol and host based on environment

enum WebSocketRequestType {
  HEARTBEAT = 'Heartbeat',
  SUBSCRIBE = 'Subscribe',
  UNSUBSCRIBE = 'Unsubscribe',
}

interface WebSocketRequest {
  [key: string]: any;
}

interface WebSocketResponse {
  [key: string]: any;
}

type SubscriptionId = string;

@Injectable({
  providedIn: 'root',
})
export class WebSocketService implements OnDestroy {
  private socket$: Option<Subject<WebSocketResponse>> = Option.none();
  private readonly messages$: Subject<WebSocketResponse> =
    new Subject<WebSocketResponse>();
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor() {
    this.connect();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();

    this.socket$.ifSome((socket) => socket.complete());
  }

  getMessages$(): Observable<WebSocketResponse> {
    return this.messages$.asObservable();
  }

  subscribe(topic: string) {
    this.send({
      Subscribe: {
        topic,
      },
    });
  }

  unsubscribe(subscriptionId: SubscriptionId) {
    this.send({
      Unsubscribe: {
        subscriptionId,
      },
    });
  }

  private send(msg: WebSocketRequest) {
    this.socket$.ifSome((socket) => socket.next(msg));
  }

  private connect() {
    console.log('Connecting to WebSocket...');

    if (this.socket$.isSome()) {
      return;
    }

    const socket: WebSocketSubject<WebSocketResponse> = webSocket({
      url: WS_ENDPOINT,
      openObserver: {
        next: () => console.log('WebSocket connection established'),
      },
      closeObserver: {
        next: () => {
          console.warn(
            'WebSocket connection closed - attempting to reconnect...',
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
          console.error('WebSocket connection error:', e);
          return EMPTY;
        }),
        takeUntil(this.destroy$),
      )
      .subscribe((msg) => this.messages$.next(msg));
  }
}
