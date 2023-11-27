import { NgModule } from '@angular/core';
import { LocationsPage } from './pages';
import { LocationsRoutingModule } from './locations-routing.module';

const PAGES = [LocationsPage];

@NgModule({
  imports: [LocationsRoutingModule],
  declarations: [...PAGES],
  exports: [],
})
export class LocationsModule {}
