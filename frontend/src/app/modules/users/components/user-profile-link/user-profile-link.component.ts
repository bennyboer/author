import {
  ChangeDetectionStrategy,
  Component,
  HostListener,
  Input,
  OnInit,
} from '@angular/core';
import { UsersService } from '../../store';
import { Observable } from 'rxjs';
import { User } from '../../models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-profile-link',
  templateUrl: './user-profile-link.component.html',
  styleUrls: ['./user-profile-link.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfileLinkComponent implements OnInit {
  @Input({ required: true })
  userId!: string;

  user$!: Observable<User>;

  constructor(
    private readonly usersService: UsersService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.usersService.loadUser(this.userId);
    this.user$ = this.usersService.getUser(this.userId);
  }

  @HostListener('click')
  navigateToUserProfile(): void {
    this.router.navigate(['/users', this.userId]);
  }
}
