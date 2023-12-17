import { Token } from '../../model';
import { Observable } from 'rxjs';

export abstract class RemoteLoginService {
  abstract login(username: string, password: string): Observable<Token>;
}
