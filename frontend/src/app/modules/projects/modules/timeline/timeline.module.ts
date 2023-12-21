import { NgModule } from '@angular/core';
import { TimelineRoutingModule } from './timeline-routing.module';
import { TimelinePage } from './pages';

const PAGES = [TimelinePage];

@NgModule({
  imports: [TimelineRoutingModule],
  declarations: [...PAGES],
  exports: [],
})
export class TimelineModule {}
