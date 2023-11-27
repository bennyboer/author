import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LocationsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: LocationsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LocationsRoutingModule {}
