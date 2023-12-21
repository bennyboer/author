import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';

export class ProjectListItem {
  readonly id: string;
  readonly name: string;

  constructor(props: { id: string; name: string }) {
    this.id = props.id;
    this.name = props.name;
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
}
