import { selectFeature } from '../../../store/selectors';
import { FEATURE_NAME } from './options';
import { createSelector } from '@ngrx/store';
import { Option } from '../../shared';
import { State } from './state';

const selectUsersState = selectFeature<State>(FEATURE_NAME);
const selectUserState = (id: string) =>
  createSelector(selectUsersState, (state) =>
    Option.someOrNone(state.users[id]),
  );
const selectUser = (id: string) =>
  createSelector(selectUserState(id), (userState) =>
    userState.map((u) => u.user),
  );
const isLoadingUser = (id: string) =>
  createSelector(selectUserState(id), (user) =>
    user.map((u) => u.loading).orElse(false),
  );

export const selectors = {
  user: selectUser,
  isLoadingUser,
};
