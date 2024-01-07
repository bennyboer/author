import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {
  catchError,
  delay,
  first,
  map,
  mergeMap,
  of,
  switchMap,
  tap,
} from 'rxjs';
import { RemoteLoginService } from './remote';
import {
  loadLoginState,
  loggedIn,
  loginFailed,
  loginStateLoaded,
  loginViaMail,
  loginViaUserName,
  logout,
  redirectAfterLoginSuccess,
} from './actions';
import { Router } from '@angular/router';
import { Option } from '../../shared';
import { LoginErrors, Token } from './state';
import { Store } from '@ngrx/store';
import { selectors } from './selectors';

@Injectable()
export class LoginStoreEffects {
  loginViaUserName$ = createEffect(() =>
    this.actions.pipe(
      ofType(loginViaUserName),
      mergeMap(({ username, password }) => {
        return this.loginService.loginViaUserName(username, password).pipe(
          map((token) => loggedIn({ token: { value: token.getValue() } })),
          catchError((error) =>
            of(
              loginFailed({
                error: LoginErrors.fromStatusCode(error.status),
              }),
            ),
          ),
          delay(500), // It's weird when the progress bar is only visible for a short time.
        );
      }),
    ),
  );

  loginViaMail$ = createEffect(() =>
    this.actions.pipe(
      ofType(loginViaMail),
      mergeMap(({ mail, password }) => {
        return this.loginService.loginViaMail(mail, password).pipe(
          map((token) => loggedIn({ token: { value: token.getValue() } })),
          catchError((error) =>
            of(
              loginFailed({
                error: LoginErrors.fromStatusCode(error.status),
              }),
            ),
          ),
          delay(500), // It's weird when the progress bar is only visible for a short time.
        );
      }),
    ),
  );

  logout$ = createEffect(
    () =>
      this.actions.pipe(
        ofType(logout),
        tap(() => localStorage.removeItem('token')),
        tap(() => this.router.navigate(['/login'])),
      ),
    { dispatch: false },
  );

  navigateToRedirectUrlAfterLogin$ = createEffect(() =>
    this.actions.pipe(
      ofType(loggedIn),
      switchMap(() =>
        this.store.select(selectors.redirectUrlAfterLogin).pipe(first()),
      ),
      tap((url) => {
        url.ifSome((redirectUrlAfterLogin) => {
          this.router.navigateByUrl(redirectUrlAfterLogin);
        });

        if (url.isNone()) {
          this.router.navigate(['/']);
        }
      }),
      map(() => redirectAfterLoginSuccess()),
    ),
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
        ofType(loggedIn),
        tap(({ token }) => localStorage.setItem('token', token.value)),
      ),
    { dispatch: false },
  );

  constructor(
    private readonly actions: Actions,
    private readonly store: Store,
    private readonly loginService: RemoteLoginService,
    private readonly router: Router,
  ) {}
}
