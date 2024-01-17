import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {
  RemoteUsersService,
  UserEventType,
  UserNameChangedEvent,
} from './remote';
import {
  loadingUserFailed,
  loadUser,
  nameUpdated,
  updateName,
  updateNameSuccess,
  updatingNameFailed,
  userLoaded,
  versionUpdated,
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

  events$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadUser),
      switchMap(({ id }) => this.remoteUsersService.getEvents(id)),
      map((event) => {
        switch (event.type) {
          case UserEventType.USERNAME_CHANGED:
            const userNameChangedEvent = event as UserNameChangedEvent;
            return nameUpdated({
              id: event.id,
              version: event.version,
              name: userNameChangedEvent.name,
            });
          default:
            return versionUpdated({
              id: event.id,
              version: event.version,
            });
        }
      }),
    ),
  );

  constructor(
    private readonly actions: Actions,
    private readonly remoteUsersService: RemoteUsersService,
  ) {}
}
