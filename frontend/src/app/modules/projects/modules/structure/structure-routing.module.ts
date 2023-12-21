import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StructurePage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: StructurePage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class StructureRoutingModule {}
