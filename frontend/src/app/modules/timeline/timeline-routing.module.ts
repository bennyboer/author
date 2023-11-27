import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TimelinePage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: TimelinePage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TimelineRoutingModule {}
