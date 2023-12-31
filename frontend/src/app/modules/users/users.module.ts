import { NgModule } from '@angular/core';
import { UserProfilePage } from './pages';
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
import { UserProfileLinkComponent } from './components';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

const PAGES = [UserProfilePage];

const COMPONENTS = [UserProfileLinkComponent];

@NgModule({
  imports: [
    CommonModule,
    UsersRoutingModule,
    StoreModule.forFeature(usersStore.featureName, usersStore.reducer),
    EffectsModule.forFeature([UsersStoreEffects]),
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
  declarations: [...PAGES, ...COMPONENTS],
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
