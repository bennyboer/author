import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { filter, map, Observable } from 'rxjs';
import { LoginError } from './state';
import { Token } from '../models';
import { Option } from '../../shared';
import { actions } from './actions';
import { selectors } from './selectors';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  constructor(private readonly store: Store) {}

  loginViaUserName(username: string, password: string): void {
    this.store.dispatch(actions.loginViaUserName({ username, password }));
  }

  loginViaMail(mail: string, password: string): void {
    this.store.dispatch(actions.loginViaMail({ mail, password }));
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

  getLoggedInUserId(): Observable<string> {
    return this.store.select(selectors.loggedInUserId).pipe(
      filter((id) => id.isSome()),
      map((id) => id.orElseThrow()),
    );
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
