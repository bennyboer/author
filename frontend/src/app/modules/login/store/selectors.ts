import { FEATURE_NAME } from './options';
import { selectFeature } from '../../../store/selectors';
import { createSelector } from '@ngrx/store';
import { Option } from '../../shared';

const loginState = selectFeature(FEATURE_NAME);
const selectToken = createSelector(loginState, (state) =>
  Option.someOrNone(state.token),
);
const selectError = createSelector(loginState, (state) => state.error);
const isLoading = createSelector(loginState, (state) => state.loading);
const isLoggedIn = createSelector(loginState, (state) => !!state.token);

export const selectors = {
  token: selectToken,
  error: selectError,
  isLoading,
  isLoggedIn,
};
