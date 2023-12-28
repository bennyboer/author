import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { loggedInGuard, loggedOutGuard, LoginModule } from './modules/login';
import { PageContainerComponent } from './components';

const routes: Routes = [
  {
    path: 'login',
    canActivate: [loggedOutGuard],
    loadChildren: () => LoginModule,
  },
  {
    path: '',
    component: PageContainerComponent,
    canActivate: [loggedInGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'projects',
      },
      {
        path: 'projects',
        loadChildren: () =>
          import('./modules/projects/projects.module').then(
            (m) => m.ProjectsModule,
          ),
      },
    ],
  },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      paramsInheritanceStrategy: 'always',
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
