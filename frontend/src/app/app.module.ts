import { APP_INITIALIZER, isDevMode, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import {
  AppComponent,
  DarkModeToggleComponent,
  HeaderComponent,
  NavigationComponent,
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
import { NavigationService, ThemeService } from './services';
import { MatMenuModule } from '@angular/material/menu';
import { UsersModule } from './modules/users/users.module';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { environment } from '../environments';

const COMPONENTS = [
  AppComponent,
  HeaderComponent,
  PageContainerComponent,
  QuickSettingsComponent,
  UserProfileDetailsComponent,
  DarkModeToggleComponent,
  NavigationComponent,
];

@NgModule({
  declarations: [...COMPONENTS],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    StoreModule.forRoot({}),
    EffectsModule.forRoot([]),
    StoreDevtoolsModule.instrument({
      maxAge: 25,
      logOnly: !environment.production,
    }),
    LoginModule,
    UsersModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    StoreDevtoolsModule.instrument({ maxAge: 25, logOnly: !isDevMode() }),
  ],
  providers: [
    ThemeService,
    NavigationService,
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    {
      provide: APP_INITIALIZER,
      deps: [ThemeService],
      multi: true,
      useFactory: (_themeService: ThemeService) => () => {},
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
