import { createSelector } from '@ngrx/store';
import { selectFeature } from '../../../store/selectors';
import { FEATURE_NAME } from './options';
import { Option } from '../../shared';
import { Project, State } from './state';

const projectsState = selectFeature<State>(FEATURE_NAME);
const selectAccessibleProjects = createSelector(
  projectsState,
  (state) => state.accessibleProjects,
);
const selectProject = (projectId: string) =>
  createSelector(projectsState, (state) =>
    Option.someOrNone(
      state.accessibleProjects.find((p: Project) => p.id === projectId),
    ),
  );
const isError = createSelector(projectsState, (state) =>
  Option.someOrNone(state.errorMessage).isSome(),
);
const isCreating = createSelector(projectsState, (state) => state.creating);
const isRemoving = createSelector(projectsState, (state) => state.removing);
const isRenaming = createSelector(projectsState, (state) => state.renaming);

export const selectors = {
  accessibleProjects: selectAccessibleProjects,
  project: selectProject,
  isError,
  isCreating,
  isRemoving,
  isRenaming,
};
