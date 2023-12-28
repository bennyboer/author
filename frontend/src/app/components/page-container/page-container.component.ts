import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  templateUrl: './page-container.component.html',
  styleUrls: ['./page-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageContainerComponent {}
