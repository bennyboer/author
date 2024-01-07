import { selectFeature } from '../../../store/selectors';
import { FEATURE_NAME } from './options';
import { createSelector } from '@ngrx/store';
import { Option } from '../../shared';

const selectUsersState = selectFeature(FEATURE_NAME);
const selectUser = (id: string) =>
  createSelector(selectUsersState, (state) =>
    Option.someOrNone(state.users[id]),
  );
const isLoadingUser = (id: string) =>
  createSelector(selectUser(id), (user) =>
    user.map((u) => u.loading).orElse(false),
  );

export const selectors = {
  user: selectUser,
  isLoadingUser,
};
