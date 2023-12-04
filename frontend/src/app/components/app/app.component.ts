import { ChangeDetectionStrategy, Component } from '@angular/core';
import { WebSocketService } from '../../modules/shared';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  constructor(private readonly webSocketService: WebSocketService) {}
}
