import { createAction, props } from '@ngrx/store';
import { Project } from './state';

export const loadAccessibleProjects = createAction(
  '[Projects] Load Accessible Projects',
);
export const accessibleProjectsLoaded = createAction(
  '[Projects] Accessible Projects Loaded',
  props<{ projects: Project[] }>(),
);
export const loadingAccessibleProjectsFailed = createAction(
  '[Projects] Loading Accessible Projects Failed',
  props<{ message: string }>(),
);

export const createProject = createAction(
  '[Projects] Create Project',
  props<{ name: string }>(),
);
export const projectCreated = createAction('[Projects] Project Created');
export const creatingProjectFailed = createAction(
  '[Projects] Creating Project Failed',
  props<{ message: string }>(),
);

export const actions = {
  loadAccessibleProjects,
  createProject,
};
