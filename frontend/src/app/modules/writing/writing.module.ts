import { NgModule } from '@angular/core';
import { WritingRoutingModule } from './writing-routing.module';
import { WritingPage } from './pages';

const PAGES = [WritingPage];

@NgModule({
  imports: [WritingRoutingModule],
  declarations: [...PAGES],
  exports: [],
})
export class WritingModule {}
