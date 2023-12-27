import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { loggedInGuard, loggedOutGuard, LoginModule } from './modules/login';

const routes: Routes = [
  {
    path: 'login',
    canActivate: [loggedOutGuard],
    loadChildren: () => LoginModule,
  },
  {
    path: '',
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
