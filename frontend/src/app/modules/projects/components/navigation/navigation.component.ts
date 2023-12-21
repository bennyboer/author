import { ChangeDetectionStrategy, Component } from '@angular/core';
import { LoginService } from '../../../login';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavigationComponent {
  constructor(private readonly loginService: LoginService) {}

  logout(): void {
    this.loginService.logout();
  }
}
