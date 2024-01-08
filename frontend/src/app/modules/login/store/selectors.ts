import { FEATURE_NAME } from './options';
import { selectFeature } from '../../../store/selectors';
import { createSelector } from '@ngrx/store';
import { Option } from '../../shared';
import { State } from './state';

const loginState = selectFeature<State>(FEATURE_NAME);
const selectToken = createSelector(loginState, (state) =>
  Option.someOrNone(state.token),
);
const selectLoggedInUserId = createSelector(loginState, (state) =>
  Option.someOrNone(state.userId),
);
const selectError = createSelector(loginState, (state) => state.error);
const isLoading = createSelector(loginState, (state) => state.loading);
const isLoggedIn = createSelector(loginState, (state) => !!state.token);
const selectRedirectUrlAfterLogin = createSelector(loginState, (state) =>
  Option.someOrNone(state.redirectUrlAfterLogin),
);

export const selectors = {
  token: selectToken,
  loggedInUserId: selectLoggedInUserId,
  error: selectError,
  isLoading,
  isLoggedIn,
  redirectUrlAfterLogin: selectRedirectUrlAfterLogin,
};
