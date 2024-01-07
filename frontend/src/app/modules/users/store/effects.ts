import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RemoteUsersService } from './remote';
import { loadingUserFailed, loadUser, userLoaded } from './actions';
import { catchError, map, of, switchMap } from 'rxjs';

@Injectable()
export class UsersStoreEffects {
  loadUser$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadUser),
      switchMap(({ id }) =>
        this.remoteUsersService.getUser(id).pipe(
          map((user) =>
            userLoaded({
              user: {
                id: user.id,
                name: user.name,
                mail: user.mail,
                firstName: user.firstName,
                lastName: user.lastName,
                imageId: user.imageId.orElse(null as any as string),
              },
            }),
          ),
          catchError((error) =>
            of(
              loadingUserFailed({
                id,
                message: error.message,
              }),
            ),
          ),
        ),
      ),
    ),
  );

  // TODO Update image

  // TODO Update user name

  // TODO Update mail

  // TODO Update password

  // TODO Rename (first name, last name)

  constructor(
    private readonly actions: Actions,
    private readonly remoteUsersService: RemoteUsersService,
  ) {}
}
