import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { WritingPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: WritingPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WritingRoutingModule {}
