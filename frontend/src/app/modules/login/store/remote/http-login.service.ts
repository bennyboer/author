import { Injectable } from '@angular/core';
import { RemoteLoginService } from './remote-login.service';
import { map, Observable } from 'rxjs';
import { Token } from '../../model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments';

interface LoginUserRequest {
  name: string;
  password: string;
}

interface AccessTokenDTO {
  value: string;
}

@Injectable()
export class HttpLoginService implements RemoteLoginService {
  constructor(private readonly http: HttpClient) {}

  login(username: string, password: string): Observable<Token> {
    const request: LoginUserRequest = {
      name: username,
      password: password,
    };

    return this.http
      .post<AccessTokenDTO>(this.url('login'), request, {
        headers: {
          UNAUTHORIZED: 'true',
        },
      })
      .pipe(map((token) => new Token({ value: token.value })));
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/users/${postfix}`;
  }
}
