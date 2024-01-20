import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import {
  MailConfirmationFailedPage,
  MailConfirmationPage,
  MailConfirmationSuccessPage,
  UserProfilePage,
} from './pages';

const routes: Routes = [
  {
    path: ':userId',
    children: [
      {
        path: '',
        pathMatch: 'full',
        component: UserProfilePage,
      },
      {
        path: 'mail',
        children: [
          {
            path: 'confirmation',
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
        ],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UsersRoutingModule {}
