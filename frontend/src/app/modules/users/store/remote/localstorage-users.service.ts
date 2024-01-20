import { Injectable } from '@angular/core';
import { RemoteUsersService } from './users.service';
import { User } from '../../models';
import { Observable, of } from 'rxjs';
import { Option } from '../../../shared';
import { UserEvent } from './events';

@Injectable()
export class LocalstorageRemoteUsersService extends RemoteUsersService {
  getEvents(id: string): Observable<UserEvent> {
    return of();
  }

  getUser(id: string): Observable<User> {
    return of(
      new User({
        id: 'TEST_USER_ID',
        version: 0,
        name: 'TEST_USER_NAME',
        mail: 'default+test@example.com',
        password: '********',
        firstName: 'Max',
        lastName: 'Mustermann',
        imageId: Option.none(),
      }),
    );
  }

  updateUserName(id: string, version: number, name: string): Observable<void> {
    return of();
  }

  updateFirstName(
    id: string,
    version: number,
    firstName: string,
  ): Observable<void> {
    return of();
  }

  updateLastName(
    id: string,
    version: number,
    lastName: string,
  ): Observable<void> {
    return of();
  }

  changePassword(
    id: string,
    version: number,
    password: string,
  ): Observable<void> {
    return of();
  }

  updateMail(id: string, version: number, mail: string): Observable<void> {
    return of();
  }
}
