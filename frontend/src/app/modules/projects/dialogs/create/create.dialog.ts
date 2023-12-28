import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnDestroy,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ProjectsService } from '../../store';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { filter, Observable, Subject, takeUntil } from 'rxjs';

@Component({
  templateUrl: './create.dialog.html',
  styleUrls: ['./create.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateDialog implements OnDestroy {
  readonly loading$: Observable<boolean> =
    this.data.projectsService.isCreating();
  private readonly destroy$: Subject<void> = new Subject<void>();

  formGroup: FormGroup = new FormGroup({
    name: new FormControl('', [Validators.required]),
  });

  constructor(
    private readonly dialogRef: MatDialogRef<CreateDialog>,
    @Inject(MAT_DIALOG_DATA)
    private readonly data: {
      projectsService: ProjectsService;
    },
  ) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  createProject(): void {
    const name = this.formGroup.get('name')?.value;
    const date = new Date();

    this.data.projectsService.createProject(name);
    this.data.projectsService
      .getAccessibleProjects()
      .pipe(
        filter((projects) =>
          projects.some(
            (project) =>
              project.createdAt.getTime() > date.getTime() &&
              project.name === name,
          ),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.dialogRef.close());
  }
}
