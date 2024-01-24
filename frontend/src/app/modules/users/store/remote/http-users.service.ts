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
  ImageUpdatedEvent,
  MailUpdatedEvent,
  PasswordChangedEvent,
  RemovedEvent,
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

interface UpdateMailRequest {
  mail: string;
}

interface UpdateImageRequest {
  imageId: string;
}

interface ConfirmMailRequest {
  mail: string;
  token: string;
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

  updateMail(id: string, version: number, mail: string): Observable<void> {
    const request: UpdateMailRequest = {
      mail,
    };

    return this.http.post<void>(this.url(`${id}/mail`), request, {
      params: {
        version,
      },
    });
  }

  confirmMail(userId: string, mail: string, token: string): Observable<void> {
    const request: ConfirmMailRequest = {
      mail,
      token,
    };

    return this.http.post<void>(
      `${environment.apiUrl}/users/${userId}/mail/confirm`,
      request,
    );
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

  updateImage(id: string, version: number, imageId: string): Observable<void> {
    const request: UpdateImageRequest = {
      imageId,
    };

    return this.http.post<void>(this.url(`${id}/image`), request, {
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

  removeUser(id: string, version: number): Observable<void> {
    return this.http.delete<void>(this.url(`${id}`), {
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
      case UserEventType.MAIL_UPDATED:
        return new MailUpdatedEvent(userId, version, payload.mail);
      case UserEventType.IMAGE_UPDATED:
        return new ImageUpdatedEvent(userId, version, payload.imageId);
      case UserEventType.REMOVED:
        return new RemovedEvent(userId, version);
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
      case 'MAIL_UPDATE_REQUESTED':
      case 'MAIL_UPDATE_CONFIRMED':
        return UserEventType.MAIL_UPDATED;
      case 'IMAGE_UPDATED':
        return UserEventType.IMAGE_UPDATED;
      case 'REMOVED':
        return UserEventType.REMOVED;
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
