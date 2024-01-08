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

  getUser(id: string): Observable<User> {
    return this.store.select(selectors.user(id)).pipe(
      filter((user) => user.isSome()),
      map((user) => user.orElseThrow()!),
      map(
        (u) =>
          new User({
            id: u.id,
            name: u.name,
            mail: u.mail,
            firstName: u.firstName,
            lastName: u.lastName,
            imageId: Option.someOrNone(u.imageId),
          }),
      ),
    );
  }
}
