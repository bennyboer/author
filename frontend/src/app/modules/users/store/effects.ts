import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {
  MailUpdatedEvent,
  PasswordChangedEvent,
  RemoteUsersService,
  RenamedFirstNameEvent,
  RenamedLastNameEvent,
  UserEventType,
  UserNameChangedEvent,
} from './remote';
import {
  changePassword,
  changePasswordSuccess,
  changingPasswordFailed,
  firstNameUpdated,
  lastNameUpdated,
  loadingUserFailed,
  loadUser,
  mailUpdated,
  nameUpdated,
  passwordChanged,
  removeUser,
  removeUserSuccess,
  removingUserFailed,
  updateFirstName,
  updateFirstNameSuccess,
  updateLastName,
  updateLastNameSuccess,
  updateMail,
  updateMailSuccess,
  updateName,
  updateNameSuccess,
  updatingFirstNameFailed,
  updatingLastNameFailed,
  updatingMailFailed,
  updatingNameFailed,
  userLoaded,
  userRemoved,
  versionUpdated,
} from './actions';
import { catchError, first, map, of, switchMap, tap } from 'rxjs';
import { LoginService } from '../../login';

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
                password: user.password,
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

  changePassword$ = createEffect(() =>
    this.actions.pipe(
      ofType(changePassword),
      switchMap(({ id, version, password }) =>
        this.remoteUsersService.changePassword(id, version, password).pipe(
          map((user) => changePasswordSuccess({ id })),
          catchError((error) =>
            of(
              changingPasswordFailed({
                id,
                message: error.message,
              }),
            ),
          ),
        ),
      ),
    ),
  );

  updateMail$ = createEffect(() =>
    this.actions.pipe(
      ofType(updateMail),
      switchMap(({ id, version, mail }) =>
        this.remoteUsersService.updateMail(id, version, mail).pipe(
          map((user) => updateMailSuccess({ id })),
          catchError((error) =>
            of(
              updatingMailFailed({
                id,
                message: error.message,
              }),
            ),
          ),
        ),
      ),
    ),
  );

  removeUser$ = createEffect(() =>
    this.actions.pipe(
      ofType(removeUser),
      switchMap(({ id, version }) =>
        this.remoteUsersService.removeUser(id, version).pipe(
          map((user) => removeUserSuccess({ id })),
          catchError((error) =>
            of(
              removingUserFailed({
                id,
                message: error.message,
              }),
            ),
          ),
        ),
      ),
    ),
  );

  onLoggedInUserRemovedLogout$ = createEffect(
    () =>
      this.actions.pipe(
        ofType(userRemoved),
        tap(({ id }) => {
          this.loginService
            .getLoggedInUserId()
            .pipe(first())
            .subscribe((loggedInUserId) => {
              if (id === loggedInUserId) {
                this.loginService.logout();
              }
            });
        }),
      ),
    {
      dispatch: false,
    },
  );

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
          case UserEventType.PASSWORD_CHANGED:
            const passwordChangedEvent = event as PasswordChangedEvent;
            return passwordChanged({
              id: event.id,
              version: event.version,
              password: passwordChangedEvent.password,
            });
          case UserEventType.MAIL_UPDATED:
            const mailUpdatedEvent = event as MailUpdatedEvent;
            return mailUpdated({
              id: event.id,
              version: event.version,
              mail: mailUpdatedEvent.mail,
            });
          case UserEventType.REMOVED:
            return userRemoved({
              id: event.id,
              version: event.version,
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
    private readonly loginService: LoginService,
  ) {}
}
