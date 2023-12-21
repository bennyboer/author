import { Observable } from 'rxjs';
import { Project, ProjectId } from '../../models';

export abstract class RemoteProjectsService {
  abstract getAccessibleProjects(): Observable<Project[]>;

  abstract createProject(name: string): Observable<void>;

  abstract removeProject(id: ProjectId): Observable<void>;

  abstract renameProject(id: ProjectId, name: string): Observable<void>;
}
