import { User } from '../../models';
import { Observable } from 'rxjs';
import { UserEvent } from './events';

export abstract class RemoteUsersService {
  abstract getEvents(id: string): Observable<UserEvent>;

  abstract getUser(id: string): Observable<User>;

  abstract renameUser(
    id: string,
    version: number,
    name: string,
  ): Observable<void>;
}
