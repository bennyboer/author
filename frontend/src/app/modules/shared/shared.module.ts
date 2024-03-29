import { NgModule } from '@angular/core';
import { CanvasComponent, EditableFieldComponent } from './components';
import { WebSocketService } from './services';
import { MatInputModule } from '@angular/material/input';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AssetComponent, AssetsService } from './assets';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  declarations: [CanvasComponent, EditableFieldComponent, AssetComponent],
  exports: [CanvasComponent, EditableFieldComponent, AssetComponent],
  providers: [WebSocketService, AssetsService],
})
export class SharedModule {}
