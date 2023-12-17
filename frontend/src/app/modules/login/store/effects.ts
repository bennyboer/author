import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of, tap } from 'rxjs';
import { RemoteLoginService } from './remote';
import {
  loadLoginState,
  login,
  loginFailure,
  loginStateLoaded,
  loginSuccess,
} from './actions';
import { Router } from '@angular/router';
import { Option } from '../../shared';
import { Token } from './state';

@Injectable()
export class LoginStoreEffects {
  login$ = createEffect(() =>
    this.actions.pipe(
      ofType(login),
      mergeMap(({ username, password }) => {
        return this.loginService.login(username, password).pipe(
          map((token) => loginSuccess({ token: { value: token.getValue() } })),
          catchError((error) =>
            of(
              loginFailure({
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  navigateToStartPage$ = createEffect(
    () =>
      this.actions.pipe(
        ofType(loginSuccess),
        tap(() => this.router.navigate(['/'])),
      ),
    { dispatch: false },
  );

  loadLoginStateFromLocalStorage$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadLoginState),
      map(() => Option.someOrNone(localStorage.getItem('token'))),
      map((token) =>
        loginStateLoaded({
          token: token
            .map((value) => ({ value }) as Token)
            .orElse(null as unknown as Token),
        }),
      ),
    ),
  );

  saveTokenToLocalStorage$ = createEffect(
    () =>
      this.actions.pipe(
        ofType(loginSuccess),
        tap(({ token }) => localStorage.setItem('token', token.value)),
      ),
    { dispatch: false },
  );

  constructor(
    private readonly actions: Actions,
    private readonly loginService: RemoteLoginService,
    private readonly router: Router,
  ) {}
}
