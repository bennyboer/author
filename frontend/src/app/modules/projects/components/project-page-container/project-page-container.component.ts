import { NavigationItem, NavigationService } from '../../../../services';
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged, map, Subject, switchMap, takeUntil } from 'rxjs';
import { ProjectsService } from '../../store';
import { Option } from '../../../shared';

@Component({
  selector: 'app-project-page-container',
  templateUrl: './project-page-container.component.html',
  styleUrls: ['./project-page-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectPageContainerComponent implements OnInit, OnDestroy {
  private pushedNavigation: boolean = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly navigationService: NavigationService,
    private readonly projectsService: ProjectsService,
  ) {}

  ngOnInit(): void {
    this.projectsService.loadAccessibleProjects();
    const projectId$ = this.route.params.pipe(
      map((params) => params['projectId']),
    );
    const project$ = projectId$.pipe(
      switchMap((projectId) => this.projectsService.getProject(projectId)),
    );
    const projectName$ = project$.pipe(
      map((project) => project.name),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
    );

    projectName$.subscribe((projectName) => this.setNavigation(projectName));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    this.navigationService.popNavigation();
  }

  private setNavigation(projectName: string): void {
    if (this.pushedNavigation) {
      this.navigationService.popNavigation();
    }

    this.navigationService.pushNavigation(
      this.route,
      Option.some('..'),
      projectName,
      [
        new NavigationItem({ label: 'Structure', link: './structure' }),
        new NavigationItem({ label: 'Characters', link: './characters' }),
        new NavigationItem({ label: 'Locations', link: './locations' }),
        new NavigationItem({ label: 'Timeline', link: './timeline' }),
        new NavigationItem({ label: 'Writing', link: './writing' }),
      ],
    );
    this.pushedNavigation = true;
  }
}
