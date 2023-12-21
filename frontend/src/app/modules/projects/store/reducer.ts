import { initialState } from './state';
import { createReducer, on } from '@ngrx/store';
import {
  accessibleProjectsLoaded,
  createProject,
  creatingProjectFailed,
  loadAccessibleProjects,
  loadingAccessibleProjectsFailed,
  projectCreated,
} from './actions';

export const reducer = createReducer(
  initialState,

  on(loadAccessibleProjects, (state) => ({ ...state, loading: true })),
  on(accessibleProjectsLoaded, (state, { projects }) => ({
    ...state,
    loading: false,
    accessibleProjects: projects,
  })),
  on(loadingAccessibleProjectsFailed, (state, { message }) => ({
    ...state,
    loading: false,
    errorMessage: message,
  })),

  on(createProject, (state) => ({ ...state, creating: true })),
  on(projectCreated, (state, { project }) => ({
    ...state,
    creating: false,
  })),
  on(creatingProjectFailed, (state, { message }) => ({
    ...state,
    creating: false,
    errorMessage: message,
  })),
);
