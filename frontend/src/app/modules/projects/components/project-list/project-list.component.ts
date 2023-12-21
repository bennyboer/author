import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { ProjectListItem } from '../project-list-item/project-list-item.component';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectListComponent {
  @Input()
  items: ProjectListItem[] = [];

  @Output()
  clicked = new EventEmitter<ProjectListItem>();
}
