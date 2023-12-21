import { NgModule } from '@angular/core';
import { ProjectsPage } from './pages';
import { ProjectsRoutingModule } from './projects-routing.module';
import {
  NavigationComponent,
  ProjectPageContainerComponent,
} from './components';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

const PAGES = [ProjectsPage];

const COMPONENTS = [NavigationComponent, ProjectPageContainerComponent];

@NgModule({
  imports: [ProjectsRoutingModule, MatIconModule, MatMenuModule],
  declarations: [...PAGES, ...COMPONENTS],
  exports: [],
})
export class ProjectsModule {}
