import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostBinding,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { FormControl } from '@angular/forms';

enum Mode {
  VIEWING = 'VIEWING',
  EDITING = 'EDITING',
}

@Component({
  selector: 'app-editable-field',
  templateUrl: './editable-field.component.html',
  styleUrls: ['./editable-field.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditableFieldComponent implements OnInit, OnDestroy {
  @Input()
  editable: boolean = true;

  @Input()
  disabled: boolean = true;

  @Input()
  set value(value: string) {
    this.ctrl.setValue(value, { emitEvent: false });
  }

  @Output()
  edited: EventEmitter<string> = new EventEmitter<string>();

  mode: Mode = Mode.VIEWING;

  ctrl: FormControl = new FormControl(null);

  protected readonly Mode = Mode;

  @HostBinding('class.readonly')
  get readonly(): boolean {
    return this.mode === Mode.VIEWING;
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {}
}
