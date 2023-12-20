import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import {
  AppComponent,
  NavigationComponent,
  PageContainerComponent,
} from './components';
import { StartPage } from './pages';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { AuthInterceptor, LoginModule } from './modules/login';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

const COMPONENTS = [AppComponent, NavigationComponent, PageContainerComponent];

const PAGES = [StartPage];

@NgModule({
  declarations: [...COMPONENTS, ...PAGES],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    StoreModule.forRoot({}),
    EffectsModule.forRoot([]),
    LoginModule,
    MatIconModule,
    MatMenuModule,
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
