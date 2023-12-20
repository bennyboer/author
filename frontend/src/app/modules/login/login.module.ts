import { NgModule } from '@angular/core';
import { LoginPage } from './pages';
import { LoginRoutingModule } from './login-routing.module';
import {
  HttpLoginService,
  LoginService,
  loginStore,
  LoginStoreEffects,
  RemoteLoginService,
} from './store';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { HttpClientModule } from '@angular/common/http';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CommonModule } from '@angular/common';

const PAGES = [LoginPage];

@NgModule({
  imports: [
    CommonModule,
    LoginRoutingModule,
    StoreModule.forFeature(loginStore.featureName, loginStore.reducer),
    EffectsModule.forFeature([LoginStoreEffects]),
    HttpClientModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  declarations: [...PAGES],
  providers: [
    LoginService,
    {
      provide: RemoteLoginService,
      useClass: HttpLoginService,
    },
  ],
})
export class LoginModule {}
