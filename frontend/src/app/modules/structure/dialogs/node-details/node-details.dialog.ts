import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';

@Component({
  templateUrl: './node-details.dialog.html',
  styleUrls: ['./node-details.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NodeDetailsDialog {
  @Input()
  label: string = '';

  @Output()
  labelChanges: EventEmitter<string> = new EventEmitter<string>();

  isEditingLabel: boolean = false;

  startEditingLabel() {
    this.isEditingLabel = true;
  }

  stopEditingLabel(newLabel: string) {
    this.isEditingLabel = false;
    this.label = newLabel;
    this.labelChanges.emit(newLabel);
  }
}
