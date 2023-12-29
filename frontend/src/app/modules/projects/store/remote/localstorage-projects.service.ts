import { EMPTY, Observable, of } from 'rxjs';
import { Project, ProjectId } from '../../models';
import { RemoteProjectsService } from './remote-projects.service';
import { Option } from '../../../shared';
import { Injectable } from '@angular/core';

@Injectable()
export class LocalStorageProjectsService extends RemoteProjectsService {
  private readonly projects: Map<ProjectId, Project> = new Map();

  override getAccessibleProjectsEvents(): Observable<void> {
    return of();
  }

  getAccessibleProjects(): Observable<Project[]> {
    return of(Array.from(this.projects.values()));
  }

  getProjectRenamedEvents(): Observable<void> {
    return of();
  }

  createProject(name: string): Observable<void> {
    const id = crypto.randomUUID();
    const project = new Project({
      id,
      version: 0,
      name,
      createdAt: new Date(),
    });

    this.projects.set(id, project);

    return EMPTY;
  }

  removeProject(id: ProjectId, version: number): Observable<void> {
    this.projects.delete(id);

    return EMPTY;
  }

  renameProject(
    id: ProjectId,
    version: number,
    name: string,
  ): Observable<void> {
    return Option.someOrNone(this.projects.get(id))
      .map((project) => new Project({ ...project, name }))
      .map((project) => this.projects.set(id, project))
      .map(() => EMPTY)
      .orElse(EMPTY);
  }

  getProject(projectId: string): Observable<Project> {
    return Option.someOrNone(this.projects.get(projectId))
      .map(of)
      .orElse(EMPTY);
  }
}
