import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { loginStore } from './index';

@Injectable()
export class LoginService {
  constructor(private readonly store: Store) {}

  login(username: string, password: string) {
    this.store.dispatch(loginStore.actions.login({ username, password }));
  }
}
