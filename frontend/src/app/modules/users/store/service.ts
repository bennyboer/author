import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { filter, map, Observable } from 'rxjs';
import { User } from '../models';
import { selectors } from './selectors';
import { actions } from './actions';
import { Option } from '../../shared';

@Injectable()
export class UsersService {
  constructor(private readonly store: Store) {}

  loadUser(id: string): void {
    this.store.dispatch(actions.loadUser({ id }));
  }

  isLoadingUser(id: string): Observable<boolean> {
    return this.store.select(selectors.isLoadingUser(id));
  }

  isError(id: string): Observable<boolean> {
    return this.store.select(selectors.isError(id));
  }

  getUser(id: string): Observable<User> {
    return this.store.select(selectors.user(id)).pipe(
      filter((user) => user.isSome()),
      map((user) => user.orElseThrow()!),
      map(
        (u) =>
          new User({
            id: u.id,
            version: u.version,
            name: u.name,
            mail: u.mail,
            password: u.password,
            firstName: u.firstName,
            lastName: u.lastName,
            imageId: Option.someOrNone(u.imageId),
          }),
      ),
    );
  }

  updateUserName(userId: string, version: number, name: string): void {
    this.store.dispatch(actions.updateName({ id: userId, version, name }));
  }

  updateFirstName(userId: string, version: number, firstName: string): void {
    this.store.dispatch(
      actions.updateFirstName({ id: userId, version, firstName }),
    );
  }

  updateLastName(userId: string, version: number, lastName: string): void {
    this.store.dispatch(
      actions.updateLastName({ id: userId, version, lastName }),
    );
  }

  changePassword(userId: string, version: number, password: string): void {
    this.store.dispatch(
      actions.changePassword({ id: userId, version, password }),
    );
  }

  updateMail(userId: string, version: number, mail: string): void {
    this.store.dispatch(actions.updateMail({ id: userId, version, mail }));
  }
}
