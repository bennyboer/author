import { Injectable } from '@angular/core';
import { RemoteUsersService } from './users.service';
import { map, Observable } from 'rxjs';
import { User } from '../../models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments';
import { Option } from '../../../shared';

interface UserDTO {
  id: string;
  name: string;
  mail: string;
  firstName: string;
  lastName: string;
  imageId?: string;
}

@Injectable()
export class HttpRemoteUsersService extends RemoteUsersService {
  constructor(private readonly http: HttpClient) {
    super();
  }

  getUser(id: string): Observable<User> {
    return this.http
      .get<UserDTO>(this.url(id))
      .pipe(map((user) => this.mapToInternalUser(user)));
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/users/${postfix}`;
  }

  private mapToInternalUser(user: UserDTO): User {
    const id = user.id;
    const name = user.name;
    const mail = user.mail;
    const firstName = user.firstName;
    const lastName = user.lastName;
    const imageId = Option.someOrNone(user.imageId);

    return new User({
      id,
      name,
      mail,
      firstName,
      lastName,
      imageId,
    });
  }
}
