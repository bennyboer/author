import { Injectable } from '@angular/core';
import { RemoteLoginService } from './remote-login.service';
import { map, Observable } from 'rxjs';
import { Token } from '../../models';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments';

interface LoginUserRequest {
  name?: string;
  mail?: string;
  password: string;
}

interface LoginUserResponse {
  token: string;
  userId: string;
}

@Injectable()
export class HttpLoginService implements RemoteLoginService {
  constructor(private readonly http: HttpClient) {}

  loginViaUserName(
    username: string,
    password: string,
  ): Observable<{ token: Token; userId: string }> {
    return this.login({
      name: username,
      password: password,
    });
  }

  loginViaMail(
    mail: string,
    password: string,
  ): Observable<{ token: Token; userId: string }> {
    return this.login({
      mail: mail,
      password: password,
    });
  }

  private login(
    request: LoginUserRequest,
  ): Observable<{ token: Token; userId: string }> {
    return this.http
      .post<LoginUserResponse>(this.url('login'), request, {
        headers: {
          UNAUTHORIZED: 'true',
        },
      })
      .pipe(
        map((response) => ({
          token: new Token({ value: response.token }),
          userId: response.userId,
        })),
      );
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/users/${postfix}`;
  }
}
