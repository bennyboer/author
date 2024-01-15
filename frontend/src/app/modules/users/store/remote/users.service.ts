import { User } from '../../models';
import { Observable } from 'rxjs';

export abstract class RemoteUsersService {
  abstract getUser(id: string): Observable<User>;

  abstract renameUser(
    id: string,
    version: number,
    name: string,
  ): Observable<void>;
}
