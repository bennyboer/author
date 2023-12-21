import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  templateUrl: './projects.page.html',
  styleUrls: ['./projects.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectsPage {}
