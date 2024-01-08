import { ChangeDetectionStrategy, Component } from '@angular/core';
import { LoginService } from '../../modules/login';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-user-profile-details',
  templateUrl: './user-profile-details.component.html',
  styleUrls: ['./user-profile-details.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfileDetailsComponent {
  constructor(private readonly loginService: LoginService) {}

  logout(): void {
    this.loginService.logout();
  }

  getUserId(): Observable<string> {
    return this.loginService.getLoggedInUserId();
  }
}
