import { NgModule } from '@angular/core';
import { StructureRoutingModule } from './structure-routing.module';
import { StructurePage } from './pages';
import { TreeGraphComponent } from './components';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { NodeDetailsDialog } from './dialogs';
import {
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogTitle,
} from '@angular/material/dialog';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import {
  HttpStructureService,
  RemoteStructureService,
  StructureService,
  structureStore,
  StructureStoreEffects,
} from './store';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { SharedModule } from '../../../shared/shared.module';

const PAGES = [StructurePage];
const COMPONENTS = [TreeGraphComponent];
const DIALOGS = [NodeDetailsDialog];

@NgModule({
  imports: [
    StructureRoutingModule,
    SharedModule,
    StoreModule.forFeature(structureStore.featureName, structureStore.reducer),
    EffectsModule.forFeature([StructureStoreEffects]),
    CommonModule,
    HttpClientModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
  ],
  declarations: [...PAGES, ...COMPONENTS, ...DIALOGS],
  providers: [
    StructureService,
    {
      provide: RemoteStructureService,
      useClass: HttpStructureService,
    },
  ],
  exports: [],
})
export class StructureModule {}
