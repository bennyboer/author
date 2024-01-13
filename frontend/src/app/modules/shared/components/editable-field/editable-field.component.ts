import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { BehaviorSubject, map, Observable, Subject, takeUntil } from 'rxjs';

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

  mode$: BehaviorSubject<Mode> = new BehaviorSubject<Mode>(Mode.VIEWING);

  ctrl: FormControl = new FormControl(null);

  protected readonly Mode = Mode;

  private originalValue: string = '';

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly renderer: Renderer2,
    private readonly elementRef: ElementRef,
  ) {}

  ngOnInit(): void {
    this.mode$.pipe(takeUntil(this.destroy$)).subscribe((mode) => {
      const isReadonly = mode === Mode.VIEWING;
      if (isReadonly) {
        this.renderer.addClass(this.elementRef.nativeElement, 'readonly');
      } else {
        this.renderer.removeClass(this.elementRef.nativeElement, 'readonly');
      }

      // TODO Do we need to focus the input field on edit mode?
    });
  }

  ngOnDestroy(): void {
    this.mode$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  enterEditMode(): void {
    this.originalValue = this.ctrl.value;
    this.mode$.next(Mode.EDITING);
  }

  submit(): void {
    this.mode$.next(Mode.VIEWING);
  }

  cancel(): void {
    this.ctrl.setValue(this.originalValue, { emitEvent: false });
    this.mode$.next(Mode.VIEWING);
  }

  isReadonly(): Observable<boolean> {
    return this.mode$.asObservable().pipe(map((mode) => mode === Mode.VIEWING));
  }

  isViewMode(): Observable<boolean> {
    return this.mode$.asObservable().pipe(map((mode) => mode === Mode.VIEWING));
  }

  isEditMode(): Observable<boolean> {
    return this.mode$.asObservable().pipe(map((mode) => mode === Mode.EDITING));
  }

  onKeyUp(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.submit();
    } else if (event.key === 'Escape') {
      this.cancel();
    }
  }
}
