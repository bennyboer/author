import { Injectable } from '@angular/core';
import { RemoteLoginService } from './remote-login.service';
import { Observable, of } from 'rxjs';
import { Token } from '../../models';

@Injectable()
export class LocalStorageLoginService implements RemoteLoginService {
  loginViaUserName(
    username: string,
    password: string,
  ): Observable<{ token: Token; userId: string }> {
    return of({ token: new Token({ value: 'TEST_TOKEN' }), userId: 'USER_ID' });
  }

  loginViaMail(
    mail: string,
    password: string,
  ): Observable<{ token: Token; userId: string }> {
    return of({ token: new Token({ value: 'TEST_TOKEN' }), userId: 'USER_ID' });
  }
}
