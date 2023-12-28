import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostBinding,
  HostListener,
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
  @HostBinding('class.project-list-item')
  readonly projectListItemClass = true;

  @Input({ required: true })
  item!: ProjectListItem;

  @Output()
  clicked = new EventEmitter<ProjectListItem>();

  @Output()
  editClicked = new EventEmitter<ProjectListItem>();

  @HostListener('click')
  onClick() {
    this.clicked.emit(this.item);
  }
}
