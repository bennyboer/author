import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  combineLatest,
  filter,
  map,
  Observable,
  Subject,
  takeUntil,
  tap,
} from 'rxjs';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ProjectsService } from '../../store';

@Component({
  templateUrl: './edit.dialog.html',
  styleUrls: ['./edit.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditDialog implements OnDestroy, OnInit {
  readonly loading$: Observable<boolean> = combineLatest([
    this.data.projectsService.isRemoving(),
    this.data.projectsService.isRenaming(),
  ]).pipe(
    tap(console.log),
    map(([isRemoving, isRenaming]) => isRemoving || isRenaming),
  );

  private readonly destroy$: Subject<void> = new Subject<void>();

  formGroup: FormGroup = new FormGroup({
    name: new FormControl('', [Validators.required]),
  });

  constructor(
    private readonly dialogRef: MatDialogRef<EditDialog>,
    @Inject(MAT_DIALOG_DATA)
    private readonly data: {
      projectsService: ProjectsService;
      project: {
        id: string;
        version: number;
        name: string;
      };
    },
  ) {}

  ngOnInit(): void {
    this.formGroup.get('name')?.reset(this.data.project.name);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  removeProject(): void {
    const { id, version } = this.data.project;
    this.data.projectsService.removeProject(id, version);
    this.data.projectsService
      .getAccessibleProjects()
      .pipe(
        filter((projects) => projects.every((project) => project.id !== id)),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.dialogRef.close());
  }

  renameProject(): void {
    const { id, version } = this.data.project;
    const name = this.formGroup.get('name')?.value;
    this.data.projectsService.renameProject(id, version, name);
    this.data.projectsService
      .getAccessibleProjects()
      .pipe(
        filter((projects) =>
          projects.some(
            (project) => project.id === id && project.name === name,
          ),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.dialogRef.close());
  }
}
