import { User } from '../../models';
import { Observable } from 'rxjs';
import { UserEvent } from './events';

export abstract class RemoteUsersService {
  abstract getEvents(id: string): Observable<UserEvent>;

  abstract getUser(id: string): Observable<User>;

  abstract updateUserName(
    id: string,
    version: number,
    name: string,
  ): Observable<void>;

  abstract updateFirstName(
    id: string,
    version: number,
    firstName: string,
  ): Observable<void>;

  abstract updateLastName(
    id: string,
    version: number,
    lastName: string,
  ): Observable<void>;

  abstract changePassword(
    id: string,
    version: number,
    password: string,
  ): Observable<void>;
}
