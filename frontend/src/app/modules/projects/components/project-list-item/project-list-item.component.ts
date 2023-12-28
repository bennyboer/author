import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';

export class ProjectListItem {
  readonly id: string;
  readonly version: number;
  readonly name: string;
  readonly createdAt: Date;

  constructor(props: {
    id: string;
    version: number;
    name: string;
    createdAt: Date;
  }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.createdAt = props.createdAt;
  }
}

@Component({
  selector: 'app-project-list-item',
  templateUrl: './project-list-item.component.html',
  styleUrls: ['./project-list-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectListItemComponent {
  @Input({ required: true })
  item!: ProjectListItem;

  @Output()
  clicked = new EventEmitter<ProjectListItem>();

  @Output()
  editClicked = new EventEmitter<ProjectListItem>();
}
