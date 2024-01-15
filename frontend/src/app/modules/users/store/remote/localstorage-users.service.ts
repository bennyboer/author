import { Injectable } from '@angular/core';
import { RemoteUsersService } from './users.service';
import { User } from '../../models';
import { Observable, of } from 'rxjs';
import { Option } from '../../../shared';

@Injectable()
export class LocalstorageRemoteUsersService extends RemoteUsersService {
  getUser(id: string): Observable<User> {
    return of(
      new User({
        id: 'TEST_USER_ID',
        version: 0,
        name: 'TEST_USER_NAME',
        mail: 'default+test@example.com',
        firstName: 'Max',
        lastName: 'Mustermann',
        imageId: Option.none(),
      }),
    );
  }

  renameUser(id: string, version: number, name: string): Observable<void> {
    return of();
  }
}
