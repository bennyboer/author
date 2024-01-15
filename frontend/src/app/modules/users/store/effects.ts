import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RemoteUsersService } from './remote';
import {
  loadingUserFailed,
  loadUser,
  updateName,
  updateNameSuccess,
  updatingNameFailed,
  userLoaded,
} from './actions';
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
                version: user.version,
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

  updateName$ = createEffect(() =>
    this.actions.pipe(
      ofType(updateName),
      switchMap(({ id, version, name }) =>
        this.remoteUsersService.renameUser(id, version, name).pipe(
          map((user) => updateNameSuccess({ id })),
          catchError((error) =>
            of(
              updatingNameFailed({
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

  // TODO Update mail

  // TODO Update password

  // TODO Rename (first name, last name)

  constructor(
    private readonly actions: Actions,
    private readonly remoteUsersService: RemoteUsersService,
  ) {}
}
