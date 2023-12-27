import { Observable } from 'rxjs';
import { Project, ProjectId } from '../../models';

export abstract class RemoteProjectsService {
  abstract getAccessibleProjectsEvents(): Observable<void>;

  abstract getAccessibleProjects(): Observable<Project[]>;

  abstract createProject(name: string): Observable<void>;

  abstract removeProject(id: ProjectId, version: number): Observable<void>;

  abstract renameProject(
    id: ProjectId,
    version: number,
    name: string,
  ): Observable<void>;
}
