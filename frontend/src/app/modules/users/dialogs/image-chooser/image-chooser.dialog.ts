import {
  ChangeDetectionStrategy,
  Component,
  HostBinding,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import {
  BehaviorSubject,
  filter,
  map,
  Observable,
  ReplaySubject,
  Subject,
  takeUntil,
} from 'rxjs';
import { Option } from '../../../shared';

@Component({
  selector: 'app-user-image-chooser-dialog',
  templateUrl: './image-chooser.dialog.html',
  styleUrls: ['./image-chooser.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageChooserDialog implements OnInit, OnDestroy {
  readonly formGroup: FormGroup = new FormGroup({
    imageFile: new FormControl(null, [Validators.required]),
  });

  readonly loading$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false,
  );

  private readonly file$: Subject<File> = new ReplaySubject(1);
  private readonly destroy$: Subject<void> = new Subject<void>();

  private isDraggedOver: boolean = false;

  ngOnInit(): void {
    const imageFile$ = this.formGroup.valueChanges.pipe(
      map((value) => value.imageFile),
      filter((imageFile) => !!imageFile),
    );

    imageFile$.pipe(takeUntil(this.destroy$)).subscribe((image) => {
      this.file$.next(image);
    });
  }

  ngOnDestroy(): void {
    this.loading$.complete();
    this.file$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostBinding('class.drag-over')
  get dragOver(): boolean {
    return this.isDraggedOver;
  }

  getImageUrl(): Observable<string> {
    return this.file$.pipe(map((file) => URL.createObjectURL(file)));
  }

  updateImage(): void {
    // TODO Base64 encode image
    // TODO Send image to server (new assets module) -> Response is an asset ID
    // TODO Send asset ID to users module as new user image ID

    console.log('updateImage', this.formGroup.value);
  }

  onImageSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.item(0);
    this.formGroup.patchValue({ imageFile: file });
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDraggedOver = false;

    Option.someOrNone(event.dataTransfer?.files?.item(0))
      .filter((f) => f.type.startsWith('image/'))
      .ifSome((f) => this.formGroup.patchValue({ imageFile: f }));
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDraggedOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDraggedOver = false;
  }
}
