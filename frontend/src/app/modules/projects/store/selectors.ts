import { createSelector } from '@ngrx/store';
import { selectFeature } from '../../../store/selectors';
import { FEATURE_NAME } from './options';
import { Option } from '../../shared';

const projectsState = selectFeature(FEATURE_NAME);
const selectAccessibleProjects = createSelector(
  projectsState,
  (state) => state.accessibleProjects,
);
const isError = createSelector(projectsState, (state) =>
  Option.someOrNone(state.errorMessage).isSome(),
);

export const selectors = {
  accessibleProjects: selectAccessibleProjects,
  isError,
};
