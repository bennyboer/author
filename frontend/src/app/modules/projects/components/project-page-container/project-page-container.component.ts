import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  templateUrl: './project-page-container.component.html',
  styleUrls: ['./project-page-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectPageContainerComponent {}
