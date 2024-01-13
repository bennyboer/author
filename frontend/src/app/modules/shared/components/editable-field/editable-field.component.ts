import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
  ViewChild,
} from '@angular/core';
import { FormControl, ValidatorFn } from '@angular/forms';
import {
  BehaviorSubject,
  combineLatest,
  distinctUntilChanged,
  filter,
  first,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { Option } from '../../util';

type EditableFieldType = 'text' | 'password';

enum Mode {
  VIEWING = 'VIEWING',
  WAITING_FOR_APPROVAL = 'WAITING_FOR_APPROVAL',
  EDITING = 'EDITING',
}

export interface EditRequest {
  newValue: string;
  approve: () => void;
  reject: () => void;
}

@Component({
  selector: 'app-editable-field',
  templateUrl: './editable-field.component.html',
  styleUrls: ['./editable-field.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditableFieldComponent implements OnInit, OnDestroy {
  @ViewChild('input', { static: true })
  input!: ElementRef;

  @Input()
  set type(value: EditableFieldType) {
    this.type$.next(value);
  }

  @Input()
  set validators(value: ValidatorFn[]) {
    this.ctrl.setValidators(value);
  }

  @Input()
  set placeholder(value: string) {
    const normalizedValue = Option.someOrNone(value).orElse('');
    this.placeholder$.next(normalizedValue);
  }

  @Input()
  set label(value: string) {
    const normalizedValue = Option.someOrNone(value).orElse('');
    this.label$.next(normalizedValue);
  }

  @Input()
  set editable(value: boolean) {
    this.editable$.next(value);
  }

  @Input()
  set disabled(value: boolean) {
    this.disabled$.next(value);
  }

  @Input()
  set value(value: string) {
    this.ctrl.setValue(value, { emitEvent: false });
  }

  @Input()
  width: string = '100%';

  @Input()
  marginBottom: string = '16px';

  @Input()
  set hint(value: string) {
    const normalizedValue = Option.someOrNone(value).orElse('');
    this.hint$.next(normalizedValue);
  }

  @Output()
  editRequested: EventEmitter<EditRequest> = new EventEmitter<EditRequest>();

  private readonly editable$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);
  private readonly disabled$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly mode$: BehaviorSubject<Mode> = new BehaviorSubject<Mode>(
    Mode.VIEWING,
  );
  private readonly placeholder$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly label$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly hint$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  private readonly type$: BehaviorSubject<EditableFieldType> =
    new BehaviorSubject<EditableFieldType>('text');

  ctrl: FormControl = new FormControl(null);

  private originalValue: string = '';

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly renderer: Renderer2,
    private readonly elementRef: ElementRef,
  ) {}

  @HostBinding('style.width')
  get componentWidth(): string {
    return this.width;
  }

  @HostBinding('style.margin-bottom')
  get componentMarginBottom(): string {
    return this.marginBottom;
  }

  ngOnInit(): void {
    this.isEditMode()
      .pipe(takeUntil(this.destroy$))
      .subscribe((isEditMode) => {
        if (isEditMode) {
          this.renderer.removeClass(this.elementRef.nativeElement, 'readonly');
          this.input.nativeElement.select();
        } else {
          this.renderer.addClass(this.elementRef.nativeElement, 'readonly');
          this.input.nativeElement.blur();
        }
      });

    this.disabled$.pipe(takeUntil(this.destroy$)).subscribe((disabled) => {
      if (disabled) {
        this.ctrl.disable({ emitEvent: false });
      } else {
        this.ctrl.enable({ emitEvent: false });
      }
    });
  }

  ngOnDestroy(): void {
    this.mode$.complete();
    this.editable$.complete();
    this.disabled$.complete();
    this.placeholder$.complete();
    this.label$.complete();
    this.hint$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  enterEditMode(): void {
    this.originalValue = this.ctrl.value;
    this.mode$.next(Mode.EDITING);
  }

  submit(): void {
    this.isEditMode()
      .pipe(
        first(),
        filter((isEditMode) => isEditMode),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        if (this.ctrl.valid) {
          const isChanged = this.ctrl.value !== this.originalValue;
          if (!isChanged) {
            this.mode$.next(Mode.VIEWING);
            return;
          }

          this.mode$.next(Mode.WAITING_FOR_APPROVAL);

          const editRequest: EditRequest = {
            newValue: this.ctrl.value,
            approve: () => {
              this.mode$.next(Mode.VIEWING);
            },
            reject: () => {
              this.mode$.next(Mode.EDITING);
            },
          };
          this.editRequested.emit(editRequest);
        }
      });
  }

  cancel(): void {
    this.isEditMode()
      .pipe(
        first(),
        filter((isEditMode) => isEditMode),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.ctrl.setValue(this.originalValue, { emitEvent: false });
        this.mode$.next(Mode.VIEWING);
      });
  }

  getMode(): Observable<Mode> {
    return this.mode$.asObservable().pipe(distinctUntilChanged());
  }

  getType(): Observable<EditableFieldType> {
    return this.type$.asObservable();
  }

  isReadonly(): Observable<boolean> {
    return this.mode$.asObservable().pipe(map((mode) => mode === Mode.VIEWING));
  }

  isViewMode(): Observable<boolean> {
    return this.getMode().pipe(map((mode) => mode === Mode.VIEWING));
  }

  isEditMode(): Observable<boolean> {
    return this.getMode().pipe(map((mode) => mode === Mode.EDITING));
  }

  isEditable(): Observable<boolean> {
    return combineLatest([this.editable$, this.disabled$]).pipe(
      map(([editable, disabled]) => editable && !disabled),
    );
  }

  getPlaceholder(): Observable<string> {
    return this.placeholder$.asObservable();
  }

  getLabel(): Observable<string> {
    return this.label$.asObservable();
  }

  getHint(): Observable<string> {
    return this.hint$.asObservable();
  }

  onKeyUp(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.submit();
    } else if (event.key === 'Escape') {
      this.cancel();
    }
  }

  @HostListener('dblclick')
  onDoubleClick(): void {
    combineLatest([this.isEditable(), this.isViewMode()])
      .pipe(
        first(),
        filter(([editable, viewMode]) => editable && viewMode),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.enterEditMode());
  }
}
