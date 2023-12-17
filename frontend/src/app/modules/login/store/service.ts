import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { loginStore } from './index';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  constructor(private readonly store: Store) {
    this.store.dispatch(loginStore.actions.loadLoginState());
  }

  login(username: string, password: string) {
    this.store.dispatch(loginStore.actions.login({ username, password }));
  }

  isLoggedIn(): Observable<boolean> {
    return this.store.select(loginStore.selectors.isLoggedIn);
  }

  isLoading(): Observable<boolean> {
    return this.store.select(loginStore.selectors.isLoading);
  }
}
