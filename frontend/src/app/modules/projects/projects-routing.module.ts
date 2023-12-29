import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { ProjectsPage } from './pages';
import { ProjectPageContainerComponent } from './components';

const routes: Routes = [
  {
    path: '',
    component: ProjectsPage,
  },
  {
    path: ':projectId',
    component: ProjectPageContainerComponent,
    children: [
      {
        path: '',
        redirectTo: 'structure',
        pathMatch: 'full',
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
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProjectsRoutingModule {}
