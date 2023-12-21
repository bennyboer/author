import { NgModule } from '@angular/core';
import { ProjectsPage } from './pages';
import { ProjectsRoutingModule } from './projects-routing.module';
import {
  NavigationComponent,
  ProjectListComponent,
  ProjectListItemComponent,
  ProjectPageContainerComponent,
} from './components';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CommonModule } from '@angular/common';
import {
  LocalStorageProjectsService,
  ProjectsService,
  RemoteProjectsService,
} from './store';

const PAGES = [ProjectsPage];

const COMPONENTS = [
  NavigationComponent,
  ProjectPageContainerComponent,
  ProjectListComponent,
  ProjectListItemComponent,
];

@NgModule({
  imports: [CommonModule, ProjectsRoutingModule, MatIconModule, MatMenuModule],
  declarations: [...PAGES, ...COMPONENTS],
  providers: [
    ProjectsService,
    {
      provide: RemoteProjectsService,
      useClass: LocalStorageProjectsService, // TODO Use HttpProjectsService instead
    },
  ],
  exports: [],
})
export class ProjectsModule {}
