import { NgModule } from '@angular/core';
import { ProjectsPage } from './pages';
import { ProjectsRoutingModule } from './projects-routing.module';
import {
  NavigationComponent,
  ProjectListComponent,
  ProjectListItemComponent,
  ProjectPageContainerComponent,
  UserProfileDetailsComponent,
} from './components';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CommonModule } from '@angular/common';
import {
  HttpProjectsService,
  ProjectsService,
  projectsStore,
  ProjectsStoreEffects,
  RemoteProjectsService,
} from './store';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { MatButtonModule } from '@angular/material/button';
import { CreateDialog, EditDialog } from './dialogs';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ReactiveFormsModule } from '@angular/forms';
import { MatProgressBarModule } from '@angular/material/progress-bar';

const PAGES = [ProjectsPage];

const COMPONENTS = [
  NavigationComponent,
  ProjectPageContainerComponent,
  ProjectListComponent,
  ProjectListItemComponent,
  UserProfileDetailsComponent,
];

const DIALOGS = [EditDialog, CreateDialog];

@NgModule({
  imports: [
    CommonModule,
    ProjectsRoutingModule,
    StoreModule.forFeature(projectsStore.featureName, projectsStore.reducer),
    EffectsModule.forFeature([ProjectsStoreEffects]),
    ReactiveFormsModule,
    MatIconModule,
    MatMenuModule,
    MatButtonModule,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  declarations: [...PAGES, ...COMPONENTS, ...DIALOGS],
  providers: [
    ProjectsService,
    {
      provide: RemoteProjectsService,
      useClass: HttpProjectsService,
    },
  ],
  exports: [],
})
export class ProjectsModule {}
