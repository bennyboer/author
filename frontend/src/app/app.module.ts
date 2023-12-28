import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import {
  AppComponent,
  DarkModeToggleComponent,
  HeaderComponent,
  PageContainerComponent,
  QuickSettingsComponent,
  UserProfileDetailsComponent,
} from './components';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { AuthInterceptor, LoginModule } from './modules/login';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ThemeService } from './services';

const COMPONENTS = [
  AppComponent,
  HeaderComponent,
  PageContainerComponent,
  QuickSettingsComponent,
  UserProfileDetailsComponent,
  DarkModeToggleComponent,
];

@NgModule({
  declarations: [...COMPONENTS],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    StoreModule.forRoot({}),
    EffectsModule.forRoot([]),
    LoginModule,
    MatIconModule,
    MatButtonModule,
  ],
  providers: [
    ThemeService,
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
