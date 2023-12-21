import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RemoteProjectsService } from './remote';
import {
  createProject,
  creatingProjectFailed,
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

  constructor(
    private readonly actions: Actions,
    private readonly projectsService: RemoteProjectsService,
  ) {}
}
