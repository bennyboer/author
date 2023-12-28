import {
  map,
  Observable,
  Subject,
  Subscription,
  switchMap,
  takeUntil,
  tap,
} from 'rxjs';
import { Project } from '../../models';
import { RemoteProjectsService } from './remote-projects.service';
import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments';
import { WebSocketService } from '../../../shared';

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
  createdAt: number;
}

@Injectable()
export class HttpProjectsService
  extends RemoteProjectsService
  implements OnDestroy
{
  private readonly accessibleProjectsEvents$ = new Subject<void>();
  private readonly events$ = new Subject<void>();
  private readonly destroy$ = new Subject<void>();
  private readonly projectRenamedEventSubscriptions = new Map<
    string,
    Subscription
  >();

  constructor(
    private readonly http: HttpClient,
    private readonly webSocketService: WebSocketService,
  ) {
    super();

    this.listenForAccessibleProjectsEvents();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    this.accessibleProjectsEvents$.complete();
  }

  getAccessibleProjectsEvents(): Observable<void> {
    return this.accessibleProjectsEvents$.asObservable();
  }

  getProjectRenamedEvents(): Observable<void> {
    return this.events$.asObservable();
  }

  getAccessibleProjects(): Observable<Project[]> {
    return this.http.get<ProjectDTO[]>(this.url('')).pipe(
      map((projects) => projects.map((project) => this.mapToProject(project))),
      tap((projects) => this.updateProjectRenamedEventSubscriptions(projects)),
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
    return this.http.post<void>(this.url(`${id}/rename`), request, {
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
      createdAt: new Date(project.createdAt * 1000),
    };
  }

  private listenForAccessibleProjectsEvents(): void {
    this.webSocketService
      .onConnected$()
      .pipe(
        switchMap(() =>
          this.webSocketService.subscribeToPermissions({
            aggregateType: 'PROJECT',
            action: 'READ',
          }),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((event) => this.accessibleProjectsEvents$.next());
  }

  private listenToProjectRenamedEvents(projectId: string): Subscription {
    return this.webSocketService
      .onConnected$()
      .pipe(
        switchMap(() =>
          this.webSocketService.subscribeTo({
            aggregateType: 'PROJECT',
            aggregateId: projectId,
            eventName: 'RENAMED',
          }),
        ),
        tap((e) => console.log(e)),
        takeUntil(this.destroy$),
      )
      .subscribe((event) => this.events$.next());
  }

  private updateProjectRenamedEventSubscriptions(projects: Project[]): void {
    const projectIds = projects.map((project) => project.id);

    for (const projectId of this.projectRenamedEventSubscriptions.keys()) {
      if (!projectIds.includes(projectId)) {
        this.projectRenamedEventSubscriptions.get(projectId)?.unsubscribe();
        this.projectRenamedEventSubscriptions.delete(projectId);
      }
    }

    for (const projectId of projectIds) {
      if (!this.projectRenamedEventSubscriptions.has(projectId)) {
        const subscription = this.listenToProjectRenamedEvents(projectId);
        this.projectRenamedEventSubscriptions.set(projectId, subscription);
      }
    }
  }
}
