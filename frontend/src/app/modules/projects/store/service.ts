import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { Project } from '../models';
import { selectors } from './selectors';
import { actions } from './actions';

@Injectable()
export class ProjectsService {
  constructor(private readonly store: Store) {}

  loadAccessibleProjects(): void {
    this.store.dispatch(actions.loadAccessibleProjects());
  }

  getAccessibleProjects(): Observable<Project[]> {
    return this.store.select(selectors.accessibleProjects);
  }

  createProject(name: string): void {
    this.store.dispatch(actions.createProject({ name }));
  }

  removeProject(id: string, version: number): void {
    this.store.dispatch(actions.removeProject({ id, version }));
  }

  renameProject(id: string, version: number, name: string): void {
    this.store.dispatch(actions.renameProject({ id, version, name }));
  }

  isCreating(): Observable<boolean> {
    return this.store.select(selectors.isCreating);
  }

  isRemoving(): Observable<boolean> {
    return this.store.select(selectors.isRemoving);
  }

  isRenaming(): Observable<boolean> {
    return this.store.select(selectors.isRenaming);
  }
}
