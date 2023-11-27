import { NgModule } from '@angular/core';
import { CanvasComponent } from './components';
import { WebSocketService } from './services';

@NgModule({
  imports: [],
  declarations: [CanvasComponent],
  exports: [CanvasComponent],
  providers: [WebSocketService],
})
export class SharedModule {}
