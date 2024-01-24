import {
  ChangeDetectionStrategy,
  Component,
  HostBinding,
  Inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import {
  BehaviorSubject,
  delay,
  filter,
  map,
  Observable,
  race,
  ReplaySubject,
  Subject,
  Subscriber,
  switchMap,
  takeUntil,
  tap,
  throwError,
} from 'rxjs';
import { AssetsService, Option } from '../../../shared';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { UsersService } from '../../store';

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

  constructor(
    private readonly dialogRef: MatDialogRef<ImageChooserDialog>,
    @Inject(MAT_DIALOG_DATA)
    private readonly data: {
      userId: string;
      version: number;
    },
    private readonly assetsService: AssetsService,
    private readonly usersService: UsersService,
  ) {}

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

  updateImage() {
    this.loading$.next(true);
    const imageFile: File = this.formGroup.value.imageFile;

    this.toBase64(imageFile)
      .pipe(
        switchMap((content) => {
          const contentType = imageFile.type;
          return this.assetsService.create(content, contentType);
        }),
        tap((assetId) =>
          this.usersService.updateImage(
            this.data.userId,
            this.data.version,
            assetId,
          ),
        ),
        switchMap((assetId) => {
          const success$ = this.usersService
            .getUserImageId(this.data.userId)
            .pipe(
              filter((imageId) =>
                imageId.map((iId) => iId === assetId).orElse(false),
              ),
            );
          const failure$ = this.usersService.isError(this.data.userId).pipe(
            filter((isError) => isError),
            switchMap(() =>
              throwError(() => new Error('Failed to update image')),
            ),
          );

          return race([success$, failure$]);
        }),
        delay(1000), // Avoid flickering
        tap({
          complete: () => this.loading$.next(false),
        }),
      )
      .subscribe(() => this.dialogRef.close());
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

  private toBase64(file: File): Observable<string> {
    return new Observable((subscriber: Subscriber<string>) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        subscriber.next(reader.result as string);
        subscriber.complete();
      };
      reader.onerror = (error) => subscriber.error(error);
    }).pipe(
      map((base64ImageWithHeader) => base64ImageWithHeader.split(',')[1]),
    );
  }
}
