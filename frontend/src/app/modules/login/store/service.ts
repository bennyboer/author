import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { map, Observable } from 'rxjs';
import { LoginError } from './state';
import { Token } from '../models';
import { Option } from '../../shared';
import { actions } from './actions';
import { selectors } from './selectors';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  constructor(private readonly store: Store) {
    this.store.dispatch(actions.loadLoginState());
  }

  login(username: string, password: string): void {
    this.store.dispatch(actions.login({ username, password }));
  }

  getToken(): Observable<Option<Token>> {
    return this.store
      .select(selectors.token)
      .pipe(map((token) => token.map((t) => new Token({ value: t.value }))));
  }

  getError(): Observable<LoginError> {
    return this.store.select(selectors.error);
  }

  isLoggedIn(): Observable<boolean> {
    return this.store.select(selectors.isLoggedIn);
  }

  isLoading(): Observable<boolean> {
    return this.store.select(selectors.isLoading);
  }

  logout(): void {
    this.store.dispatch(actions.logout());
  }

  redirectAfterLogin(url: string): void {
    this.store.dispatch(actions.redirectAfterLogin({ url }));
  }
}
