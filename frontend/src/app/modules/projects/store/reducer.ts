import { initialState } from './state';
import { createReducer, on } from '@ngrx/store';
import {
  accessibleProjectsLoaded,
  createProject,
  creatingProjectFailed,
  loadAccessibleProjects,
  loadingAccessibleProjectsFailed,
  projectCreated,
  projectRemoved,
  projectRenamed,
  removeProject,
  removingProjectFailed,
  renameProject,
  renamingProjectFailed,
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
  on(projectCreated, (state) => ({
    ...state,
    creating: false,
  })),
  on(creatingProjectFailed, (state, { message }) => ({
    ...state,
    creating: false,
    errorMessage: message,
  })),

  on(removeProject, (state) => ({ ...state, removing: true })),
  on(projectRemoved, (state) => ({
    ...state,
    removing: false,
  })),
  on(removingProjectFailed, (state, { message }) => ({
    ...state,
    removing: false,
    errorMessage: message,
  })),

  on(renameProject, (state) => ({ ...state, renaming: true })),
  on(projectRenamed, (state) => ({
    ...state,
    renaming: false,
  })),
  on(renamingProjectFailed, (state, { message }) => ({
    ...state,
    renaming: false,
    errorMessage: message,
  })),
);
