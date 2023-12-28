import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RemoteProjectsService } from './remote';
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
import { catchError, map, mergeMap, of } from 'rxjs';

@Injectable()
export class ProjectsStoreEffects {
  createProject$ = createEffect(() =>
    this.actions.pipe(
      ofType(createProject),
      mergeMap(({ name }) => {
        return this.projectsService.createProject(name).pipe(
          map(() => projectCreated()),
          catchError((error) =>
            of(
              creatingProjectFailed({
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  removeProject$ = createEffect(() =>
    this.actions.pipe(
      ofType(removeProject),
      mergeMap(({ id, version }) => {
        return this.projectsService.removeProject(id, version).pipe(
          map(() => projectRemoved()),
          catchError((error) =>
            of(
              removingProjectFailed({
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  renameProject$ = createEffect(() =>
    this.actions.pipe(
      ofType(renameProject),
      mergeMap(({ id, version, name }) => {
        return this.projectsService.renameProject(id, version, name).pipe(
          map(() => projectRenamed()),
          catchError((error) =>
            of(
              renamingProjectFailed({
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  loadAccessibleProjects$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadAccessibleProjects),
      mergeMap(() => {
        return this.projectsService.getAccessibleProjects().pipe(
          map((projects) => accessibleProjectsLoaded({ projects })),
          catchError((error) =>
            of(loadingAccessibleProjectsFailed({ message: error.message })),
          ),
        );
      }),
    ),
  );

  reloadAccessibleProjectsOnAccessibleProjectsEvent$ = createEffect(() =>
    this.projectsService
      .getAccessibleProjectsEvents()
      .pipe(map(() => loadAccessibleProjects())),
  );

  reloadAccessibleProjectsOnProjectRenamedEvent$ = createEffect(() =>
    this.projectsService
      .getProjectRenamedEvents()
      .pipe(map(() => loadAccessibleProjects())),
  );

  constructor(
    private readonly actions: Actions,
    private readonly projectsService: RemoteProjectsService,
  ) {}
}
