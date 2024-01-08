import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { NavigationService } from '../../../../services';
import { ActivatedRoute } from '@angular/router';
import { Option } from '../../../shared';

@Component({
  selector: 'app-user-profile-page',
  templateUrl: './user-profile.page.html',
  styleUrls: ['./user-profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfilePage implements OnInit, OnDestroy {
  constructor(
    private readonly route: ActivatedRoute,
    private readonly navigationService: NavigationService,
  ) {}

  ngOnInit(): void {
    this.navigationService.pushNavigation(
      this.route,
      Option.some('/'),
      'User Profile',
      [],
    );
  }

  ngOnDestroy(): void {
    this.navigationService.popNavigation();
  }
}
