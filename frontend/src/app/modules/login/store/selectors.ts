import { FEATURE_NAME } from './options';
import { selectFeature } from '../../../store/selectors';
import { createSelector } from '@ngrx/store';
import { Option } from '../../shared';

const loginState = selectFeature(FEATURE_NAME);
const selectToken = createSelector(loginState, (state) =>
  Option.someOrNone(state.token),
);
const selectErrorMessage = createSelector(loginState, (state) =>
  Option.someOrNone(state.errorMessage),
);
const isLoading = createSelector(loginState, (state) => state.loading);

export const selectors = {
  token: selectToken,
  errorMessage: selectErrorMessage,
  isLoading,
};
