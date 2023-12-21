import { Injectable } from '@angular/core';
import { RemoteLoginService } from './remote-login.service';
import { Observable, of } from 'rxjs';
import { Token } from '../../models';

@Injectable()
export class LocalStorageLoginService implements RemoteLoginService {
  login(_username: string, _password: string): Observable<Token> {
    return of(new Token({ value: 'TEST_TOKEN' }));
  }
}
