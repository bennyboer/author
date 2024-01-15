import { Injectable } from '@angular/core';
import { RemoteUsersService } from './users.service';
import { map, Observable } from 'rxjs';
import { User } from '../../models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments';
import { Option } from '../../../shared';

interface UserDTO {
  id: string;
  version: number;
  name: string;
  mail: string;
  firstName: string;
  lastName: string;
  imageId?: string;
}

interface RenameUserRequest {
  name: string;
}

@Injectable()
export class HttpRemoteUsersService extends RemoteUsersService {
  constructor(private readonly http: HttpClient) {
    super();
  }

  // TODO Get events

  getUser(id: string): Observable<User> {
    return this.http
      .get<UserDTO>(this.url(id))
      .pipe(map((user) => this.mapToInternalUser(user)));
  }

  renameUser(id: string, version: number, name: string): Observable<void> {
    const request: RenameUserRequest = {
      name,
    };

    return this.http.post<void>(this.url(`${id}/rename`), request, {
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
      firstName,
      lastName,
      imageId,
    });
  }
}
