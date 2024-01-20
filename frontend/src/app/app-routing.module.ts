import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { loggedInGuard, loggedOutGuard, LoginModule } from './modules/login';
import { PageContainerComponent } from './components';
import {
  MailConfirmationFailedPage,
  MailConfirmationPage,
  MailConfirmationSuccessPage,
} from './modules/users';

const routes: Routes = [
  {
    path: 'login',
    canActivate: [loggedOutGuard],
    loadChildren: () => LoginModule,
  },
  {
    path: 'users/mail/confirmation',
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: MailConfirmationPage,
      },
      {
        path: 'success',
        component: MailConfirmationSuccessPage,
      },
      {
        path: 'failed',
        component: MailConfirmationFailedPage,
      },
    ],
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
      {
        path: 'users',
        loadChildren: () =>
          import('./modules/users/users.module').then((m) => m.UsersModule),
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
