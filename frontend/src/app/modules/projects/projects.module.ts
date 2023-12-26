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
  HttpProjectsService,
  ProjectsService,
  projectsStore,
  ProjectsStoreEffects,
  RemoteProjectsService,
} from './store';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';

const PAGES = [ProjectsPage];

const COMPONENTS = [
  NavigationComponent,
  ProjectPageContainerComponent,
  ProjectListComponent,
  ProjectListItemComponent,
];

@NgModule({
  imports: [
    CommonModule,
    ProjectsRoutingModule,
    StoreModule.forFeature(projectsStore.featureName, projectsStore.reducer),
    EffectsModule.forFeature([ProjectsStoreEffects]),
    MatIconModule,
    MatMenuModule,
  ],
  declarations: [...PAGES, ...COMPONENTS],
  providers: [
    ProjectsService,
    {
      provide: RemoteProjectsService,
      useClass: HttpProjectsService,
    },
  ],
  exports: [],
})
export class ProjectsModule {}
