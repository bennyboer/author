import { ChangeDetectionStrategy, Component } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ProjectListItem } from '../../components';
import { ProjectsService } from '../../store';
import { Router } from '@angular/router';

@Component({
  templateUrl: './projects.page.html',
  styleUrls: ['./projects.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectsPage {
  projectListItems$: Observable<ProjectListItem[]> = this.projectsService
    .getAccessibleProjects()
    .pipe(
      map((projects) =>
        projects.map((project) => ({
          id: project.id,
          name: project.name,
        })),
      ),
    );

  constructor(
    private readonly projectsService: ProjectsService,
    private readonly router: Router,
  ) {
    this.projectsService.loadAccessibleProjects();
  }

  createNewProject(): void {
    this.projectsService.createProject('Test');
  }

  navigateToProject(item: ProjectListItem) {
    this.router.navigate(['projects', item.id]);
  }
}
