import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-user-profile-page',
  templateUrl: './user-profile.page.html',
  styleUrls: ['./user-profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfilePage {}
