import { Token } from '../../models';
import { Observable } from 'rxjs';

export abstract class RemoteLoginService {
  abstract loginViaUserName(
    username: string,
    password: string,
  ): Observable<{ token: Token; userId: string }>;

  abstract loginViaMail(
    mail: string,
    password: string,
  ): Observable<{ token: Token; userId: string }>;
}
