import { NgModule } from '@angular/core';
import {
  MailConfirmationFailedPage,
  MailConfirmationPage,
  MailConfirmationSuccessPage,
  UserProfilePage,
} from './pages';
import { UsersRoutingModule } from './users-routing.module';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import {
  HttpRemoteUsersService,
  RemoteUsersService,
  UsersService,
  usersStore,
  UsersStoreEffects,
} from './store';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { MatButtonModule } from '@angular/material/button';
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
import {
  UserProfileImageComponent,
  UserProfileLinkComponent,
} from './components';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SharedModule } from '../shared/shared.module';
import { ImageChooserDialog } from './dialogs';

const PAGES = [
  UserProfilePage,
  MailConfirmationPage,
  MailConfirmationSuccessPage,
  MailConfirmationFailedPage,
];

const COMPONENTS = [UserProfileLinkComponent, UserProfileImageComponent];

const DIALOGS = [ImageChooserDialog];

@NgModule({
  imports: [
    CommonModule,
    UsersRoutingModule,
    StoreModule.forFeature(usersStore.featureName, usersStore.reducer),
    EffectsModule.forFeature([UsersStoreEffects]),
    SharedModule,
    ReactiveFormsModule,
    MatIconModule,
    MatButtonModule,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
  ],
  declarations: [...PAGES, ...COMPONENTS, ...DIALOGS],
  providers: [
    UsersService,
    {
      provide: RemoteUsersService,
      useClass: HttpRemoteUsersService,
    },
  ],
  exports: [UserProfileLinkComponent],
})
export class UsersModule {}
