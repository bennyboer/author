import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { loginStore } from './index';
import { map, Observable } from 'rxjs';
import { LoginError } from './state';
import { Token } from '../model';
import { Option } from '../../shared';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  constructor(private readonly store: Store) {
    this.store.dispatch(loginStore.actions.loadLoginState());
  }

  login(username: string, password: string): void {
    this.store.dispatch(loginStore.actions.login({ username, password }));
  }

  getToken(): Observable<Option<Token>> {
    return this.store
      .select(loginStore.selectors.token)
      .pipe(map((token) => token.map((t) => new Token({ value: t.value }))));
  }

  getError(): Observable<LoginError> {
    return this.store.select(loginStore.selectors.error);
  }

  isLoggedIn(): Observable<boolean> {
    return this.store.select(loginStore.selectors.isLoggedIn);
  }

  isLoading(): Observable<boolean> {
    return this.store.select(loginStore.selectors.isLoading);
  }

  logout(): void {
    this.store.dispatch(loginStore.actions.logout());
  }

  redirectAfterLogin(url: string): void {
    this.store.dispatch(loginStore.actions.redirectAfterLogin({ url }));
  }
}
