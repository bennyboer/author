import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent, NavigationComponent } from './components';
import { StartPage } from './pages';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

const COMPONENTS = [AppComponent, NavigationComponent];

const PAGES = [StartPage];

@NgModule({
  declarations: [...COMPONENTS, ...PAGES],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    StoreModule.forRoot({}),
    EffectsModule.forRoot([]),
    MatIconModule,
    MatMenuModule,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
