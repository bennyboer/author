import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { UserProfilePage } from './pages';

const routes: Routes = [
  {
    path: ':userId',
    component: UserProfilePage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UsersRoutingModule {}
