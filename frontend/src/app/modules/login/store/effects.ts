import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { RemoteLoginService } from './remote';
import { login, loginFailure, loginSuccess } from './actions';

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

  constructor(
    private readonly actions: Actions,
    private readonly loginService: RemoteLoginService,
  ) {}
}
