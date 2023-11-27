import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  templateUrl: './characters.page.html',
  styleUrls: ['./characters.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CharactersPage {}
