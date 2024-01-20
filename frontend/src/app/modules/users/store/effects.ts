import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {
  RemoteUsersService,
  RenamedFirstNameEvent,
  RenamedLastNameEvent,
  UserEventType,
  UserNameChangedEvent,
} from './remote';
import {
  firstNameUpdated,
  lastNameUpdated,
  loadingUserFailed,
  loadUser,
  nameUpdated,
  updateFirstName,
  updateFirstNameSuccess,
  updateLastName,
  updateLastNameSuccess,
  updateName,
  updateNameSuccess,
  updatingFirstNameFailed,
  updatingLastNameFailed,
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
        this.remoteUsersService.updateUserName(id, version, name).pipe(
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

  updateFirstName$ = createEffect(() =>
    this.actions.pipe(
      ofType(updateFirstName),
      switchMap(({ id, version, firstName }) =>
        this.remoteUsersService.updateFirstName(id, version, firstName).pipe(
          map((user) => updateFirstNameSuccess({ id })),
          catchError((error) =>
            of(
              updatingFirstNameFailed({
                id,
                message: error.message,
              }),
            ),
          ),
        ),
      ),
    ),
  );

  updateLastName$ = createEffect(() =>
    this.actions.pipe(
      ofType(updateLastName),
      switchMap(({ id, version, lastName }) =>
        this.remoteUsersService.updateLastName(id, version, lastName).pipe(
          map((user) => updateLastNameSuccess({ id })),
          catchError((error) =>
            of(
              updatingLastNameFailed({
                id,
                message: error.message,
              }),
            ),
          ),
        ),
      ),
    ),
  );

  // TODO Update mail

  // TODO Update password

  // TODO Update image

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
          case UserEventType.RENAMED_FIRST_NAME:
            const renamedFirstNameEvent = event as RenamedFirstNameEvent;
            return firstNameUpdated({
              id: event.id,
              version: event.version,
              firstName: renamedFirstNameEvent.firstName,
            });
          case UserEventType.RENAMED_LAST_NAME:
            const renamedLastNameEvent = event as RenamedLastNameEvent;
            return lastNameUpdated({
              id: event.id,
              version: event.version,
              lastName: renamedLastNameEvent.lastName,
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
