import { map, Observable } from 'rxjs';
import { Project } from '../../models';
import { RemoteProjectsService } from './remote-projects.service';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments';

interface CreateProjectRequest {
  name: string;
}

interface RenameProjectRequest {
  name: string;
}

interface ProjectDTO {
  id: string;
  version: number;
  name: string;
  createdAt: Date;
}

@Injectable()
export class HttpProjectsService extends RemoteProjectsService {
  constructor(private readonly http: HttpClient) {
    super();
    // TODO Connect to websocket for tracking permission changes
  }

  getAccessibleProjects(): Observable<Project[]> {
    return this.http
      .get<ProjectDTO[]>(this.url(''))
      .pipe(
        map((projects) =>
          projects.map((project) => this.mapToProject(project)),
        ),
      );
  }

  createProject(name: string): Observable<void> {
    const request: CreateProjectRequest = { name };
    return this.http.post<void>(this.url(''), request);
  }

  removeProject(id: string, version: number): Observable<void> {
    return this.http.delete<void>(this.url(id), {
      params: { version },
    });
  }

  renameProject(id: string, version: number, name: string): Observable<void> {
    const request: RenameProjectRequest = { name };
    return this.http.put<void>(this.url(id), request, {
      params: { version },
    });
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/projects/${postfix}`;
  }

  private mapToProject(project: ProjectDTO): Project {
    return {
      id: project.id,
      version: project.version,
      name: project.name,
    };
  }
}
