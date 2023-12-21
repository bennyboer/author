import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CharactersPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: CharactersPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CharactersRoutingModule {}
