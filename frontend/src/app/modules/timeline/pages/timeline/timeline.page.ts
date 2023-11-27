import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  templateUrl: './timeline.page.html',
  styleUrls: ['./timeline.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TimelinePage {}
