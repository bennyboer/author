import { Injectable, OnDestroy } from '@angular/core';
import { RemoteUsersService } from './users.service';
import {
  map,
  Observable,
  Subject,
  Subscription,
  switchMap,
  takeUntil,
} from 'rxjs';
import { User } from '../../models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments';
import { Option, WebSocketService } from '../../../shared';
import {
  PasswordChangedEvent,
  RenamedFirstNameEvent,
  RenamedLastNameEvent,
  UserEvent,
  UserEventType,
  UserNameChangedEvent,
} from './events';
import { EventMessage } from '../../../shared/services';

interface UserDTO {
  id: string;
  version: number;
  name: string;
  mail: string;
  firstName: string;
  lastName: string;
  imageId?: string;
}

interface UpdateUserNameRequest {
  name: string;
}

interface RenameFirstNameRequest {
  firstName: string;
}

interface RenameLastNameRequest {
  lastName: string;
}

interface ChangePasswordRequest {
  password: string;
}

type UserId = string;

@Injectable()
export class HttpRemoteUsersService
  extends RemoteUsersService
  implements OnDestroy
{
  private readonly userEventsSubscriptions: Map<UserId, Subscription> = new Map<
    UserId,
    Subscription
  >();
  private readonly events$: Subject<EventMessage> = new Subject<EventMessage>();
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly http: HttpClient,
    private readonly webSocketService: WebSocketService,
  ) {
    super();
  }

  ngOnDestroy(): void {
    this.events$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getEvents(id: UserId): Observable<UserEvent> {
    this.listenToUserEvents(id);

    return this.events$.pipe(map((event) => this.mapToUserEvent(event)));
  }

  getUser(id: UserId): Observable<User> {
    return this.http
      .get<UserDTO>(this.url(id))
      .pipe(map((user) => this.mapToInternalUser(user)));
  }

  updateUserName(id: UserId, version: number, name: string): Observable<void> {
    const request: UpdateUserNameRequest = {
      name,
    };

    return this.http.post<void>(this.url(`${id}/username`), request, {
      params: {
        version,
      },
    });
  }

  updateFirstName(
    id: UserId,
    version: number,
    firstName: string,
  ): Observable<void> {
    const request: RenameFirstNameRequest = {
      firstName,
    };

    return this.http.post<void>(this.url(`${id}/rename/firstname`), request, {
      params: {
        version,
      },
    });
  }

  updateLastName(
    id: UserId,
    version: number,
    lastName: string,
  ): Observable<void> {
    const request: RenameLastNameRequest = {
      lastName,
    };

    return this.http.post<void>(this.url(`${id}/rename/lastname`), request, {
      params: {
        version,
      },
    });
  }

  changePassword(
    id: string,
    version: number,
    password: string,
  ): Observable<void> {
    const request: ChangePasswordRequest = {
      password,
    };

    return this.http.post<void>(this.url(`${id}/password`), request, {
      params: {
        version,
      },
    });
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/users/${postfix}`;
  }

  private mapToInternalUser(user: UserDTO): User {
    const id = user.id;
    const version = user.version;
    const name = user.name;
    const mail = user.mail;
    const firstName = user.firstName;
    const lastName = user.lastName;
    const imageId = Option.someOrNone(user.imageId);

    return new User({
      id,
      version,
      name,
      mail,
      password: '********',
      firstName,
      lastName,
      imageId,
    });
  }

  private mapToUserEvent(msg: EventMessage): UserEvent {
    const userId = msg.topic.aggregateId;
    const version = msg.topic.version;

    const type = this.mapToUserEventType(msg.eventName);
    const payload = msg.payload;

    switch (type) {
      case UserEventType.USERNAME_CHANGED:
        return new UserNameChangedEvent(userId, version, payload.newName);
      case UserEventType.RENAMED_FIRST_NAME:
        return new RenamedFirstNameEvent(userId, version, payload.firstName);
      case UserEventType.RENAMED_LAST_NAME:
        return new RenamedLastNameEvent(userId, version, payload.lastName);
      case UserEventType.PASSWORD_CHANGED:
        return new PasswordChangedEvent(userId, version, '********');
      default:
        return {
          type,
          id: userId,
          version,
        };
    }
  }

  private mapToUserEventType(eventName: string): UserEventType {
    switch (eventName) {
      case 'USERNAME_CHANGED':
        return UserEventType.USERNAME_CHANGED;
      case 'RENAMED_FIRST_NAME':
        return UserEventType.RENAMED_FIRST_NAME;
      case 'RENAMED_LAST_NAME':
        return UserEventType.RENAMED_LAST_NAME;
      case 'PASSWORD_CHANGED':
        return UserEventType.PASSWORD_CHANGED;
      default:
        return UserEventType.OTHER;
    }
  }

  private listenToUserEvents(id: UserId): Subscription {
    if (this.userEventsSubscriptions.has(id)) {
      return this.userEventsSubscriptions.get(id)!;
    }

    const subscription = this.webSocketService
      .onConnected$()
      .pipe(
        switchMap(() =>
          this.webSocketService.subscribeTo({
            aggregateType: 'USER',
            aggregateId: id,
          }),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((event) => this.events$.next(event));

    this.userEventsSubscriptions.set(id, subscription);

    return subscription;
  }
}
