import { FEATURE_NAME } from './options';
import { createSelector } from '@ngrx/store';
import { selectFeature } from '../../../../../store/selectors';
import { Option } from '../../../../shared';
import { State } from './state';

const structureState = selectFeature<State>(FEATURE_NAME);
const selectStructure = createSelector(structureState, (state: State) =>
  Option.someOrNone(state.structure),
);
const selectIsFailure = createSelector(structureState, (state: State) =>
  Option.someOrNone(state.errorMessage).isSome(),
);

export const selectors = {
  structure: selectStructure,
  isFailure: selectIsFailure,
};
