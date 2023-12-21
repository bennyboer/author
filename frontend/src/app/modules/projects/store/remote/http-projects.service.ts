import { Observable } from 'rxjs';
import { Project } from '../../models';
import { RemoteProjectsService } from './remote-projects.service';
import { Injectable } from '@angular/core';

@Injectable()
export class HttpProjectsService extends RemoteProjectsService {
  getAccessibleProjects(): Observable<Project[]> {
    throw new Error('Method not implemented.'); // TODO
  }

  createProject(name: string): Observable<void> {
    throw new Error('Method not implemented.'); // TODO
  }

  removeProject(id: string): Observable<void> {
    throw new Error('Method not implemented.'); // TODO
  }

  renameProject(id: string, name: string): Observable<void> {
    throw new Error('Method not implemented.'); // TODO
  }
}
