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

  reloadAccessibleProjectsOnEvent$ = createEffect(() =>
    this.projectsService
      .getAccessibleProjectsEvents()
      .pipe(map(() => loadAccessibleProjects())),
  );

  constructor(
    private readonly actions: Actions,
    private readonly projectsService: RemoteProjectsService,
  ) {}
}
