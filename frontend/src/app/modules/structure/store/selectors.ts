import { FEATURE_NAME } from './options';
import { createSelector } from '@ngrx/store';
import { selectFeature } from '../../../store/selectors';
import { Option } from '../../shared';

const structureState = selectFeature(FEATURE_NAME);
const selectTree = createSelector(structureState, (state) => state.tree);
const selectIsFailure = createSelector(structureState, (state) =>
  Option.someOrNone(state.errorMessage).isSome(),
);

export const selectors = {
  tree: selectTree,
  isFailure: selectIsFailure,
};
