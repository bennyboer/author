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
const isCreating = createSelector(projectsState, (state) => state.creating);
const isRemoving = createSelector(projectsState, (state) => state.removing);
const isRenaming = createSelector(projectsState, (state) => state.renaming);

export const selectors = {
  accessibleProjects: selectAccessibleProjects,
  isError,
  isCreating,
  isRemoving,
  isRenaming,
};
