import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StartPage } from './pages';
import { PageContainerComponent } from './components';

const routes: Routes = [
  {
    path: 'login',
    loadChildren: () =>
      import('./modules/login/login.module').then((m) => m.LoginModule),
  },
  {
    path: '',
    component: PageContainerComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'start',
      },
      {
        path: 'start',
        component: StartPage,
      },
      {
        path: 'structure',
        loadChildren: () =>
          import('./modules/structure/structure.module').then(
            (m) => m.StructureModule,
          ),
      },
      {
        path: 'characters',
        loadChildren: () =>
          import('./modules/characters/characters.module').then(
            (m) => m.CharactersModule,
          ),
      },
      {
        path: 'writing',
        loadChildren: () =>
          import('./modules/writing/writing.module').then(
            (m) => m.WritingModule,
          ),
      },
      {
        path: 'timeline',
        loadChildren: () =>
          import('./modules/timeline/timeline.module').then(
            (m) => m.TimelineModule,
          ),
      },
      {
        path: 'locations',
        loadChildren: () =>
          import('./modules/locations/locations.module').then(
            (m) => m.LocationsModule,
          ),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
