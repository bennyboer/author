import { ChangeDetectionStrategy, Component } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ProjectListItem } from '../../components';
import { ProjectsService } from '../../store';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { CreateDialog, EditDialog } from '../../dialogs';

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
        projects
          .map(
            (project) =>
              new ProjectListItem({
                id: project.id,
                version: project.version,
                name: project.name,
                createdAt: project.createdAt,
              }),
          )
          .sort((a, b) => a.createdAt.getTime() - b.createdAt.getTime()),
      ),
    );

  constructor(
    private readonly projectsService: ProjectsService,
    private readonly router: Router,
    private readonly dialog: MatDialog,
  ) {
    this.projectsService.loadAccessibleProjects();
  }

  createNewProject(): void {
    this.dialog.open(CreateDialog, {
      data: {
        projectsService: this.projectsService,
      },
      width: '400px',
    });
  }

  editProject(item: ProjectListItem) {
    this.dialog.open(EditDialog, {
      data: {
        projectsService: this.projectsService,
        project: {
          id: item.id,
          version: item.version,
          name: item.name,
        },
      },
      width: '400px',
    });
  }

  navigateToProject(item: ProjectListItem) {
    this.router.navigate(['projects', item.id]);
  }
}
