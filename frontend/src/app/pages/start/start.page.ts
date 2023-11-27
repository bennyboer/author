import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  styleUrls: ['./start.page.scss'],
  templateUrl: './start.page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StartPage {}
