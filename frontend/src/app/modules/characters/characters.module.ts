import { NgModule } from '@angular/core';
import { CharactersRoutingModule } from './characters-routing.module';
import { CharactersPage } from './pages';

const PAGES = [CharactersPage];

@NgModule({
  imports: [CharactersRoutingModule],
  declarations: [...PAGES],
  exports: [],
})
export class CharactersModule {}
