import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { NavigationService } from '../../../../services';
import { ActivatedRoute } from '@angular/router';
import { Option } from '../../../shared';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { UsersService } from '../../store';
import {
  filter,
  first,
  map,
  Observable,
  of,
  ReplaySubject,
  Subject,
  switchMap,
  takeUntil,
  tap,
} from 'rxjs';
import { User } from '../../models';
import { EditRequest } from '../../../shared/components/editable-field/editable-field.component';

@Component({
  selector: 'app-user-profile-page',
  templateUrl: './user-profile.page.html',
  styleUrls: ['./user-profile.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfilePage implements OnInit, OnDestroy {
  readonly deleteUserProfileFormGroup: FormGroup = new FormGroup({
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
    ]),
  });

  private readonly user$: Subject<User> = new ReplaySubject<User>(1);
  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly Validators = Validators;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly navigationService: NavigationService,
    private readonly usersService: UsersService,
  ) {}

  ngOnInit(): void {
    const userId$ = this.route.params.pipe(
      map((params) => params['userId']),
      filter((userId) => !!userId),
      first(),
    );

    userId$
      .pipe(
        tap((userId) => this.usersService.loadUser(userId)),
        switchMap((userId) => this.usersService.getUser(userId)),
        takeUntil(this.destroy$),
      )
      .subscribe((user) => this.user$.next(user));

    this.navigationService.pushNavigation(
      this.route,
      Option.some('/'),
      'User Profile',
      [],
    );
  }

  ngOnDestroy(): void {
    this.user$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.navigationService.popNavigation();
  }

  getUserId(): Observable<string> {
    return this.user$.pipe(map((user) => user.id));
  }

  getUserName(): Observable<string> {
    return this.user$.pipe(map((user) => user.name));
  }

  getMail(): Observable<string> {
    return this.user$.pipe(map((user) => user.mail));
  }

  getPassword(): Observable<string> {
    return of('********');
  }

  getFirstName(): Observable<string> {
    return this.user$.pipe(map((user) => user.firstName));
  }

  getLastName(): Observable<string> {
    return this.user$.pipe(map((user) => user.lastName));
  }

  updateUserName(request: EditRequest): void {
    console.log('updateUserName', request.newValue); // TODO

    setTimeout(() => {
      Math.random() > 0.5 ? request.reject() : request.approve();
    }, 1000);
  }

  updateMail(request: EditRequest): void {
    console.log('updateMail', request.newValue); // TODO

    setTimeout(() => {
      Math.random() > 0.5 ? request.reject() : request.approve();
    }, 1000);
  }

  updatePassword(request: EditRequest): void {
    console.log('updatePassword', request.newValue); // TODO

    setTimeout(() => {
      Math.random() > 0.5 ? request.reject() : request.approve();
    }, 1000);
  }

  updateFirstName(request: EditRequest): void {
    console.log('updateFirstName', request.newValue); // TODO

    setTimeout(() => {
      Math.random() > 0.5 ? request.reject() : request.approve();
    }, 1000);
  }

  updateLastName(request: EditRequest): void {
    console.log('updateLastName', request.newValue); // TODO

    setTimeout(() => {
      Math.random() > 0.5 ? request.reject() : request.approve();
    }, 1000);
  }

  deleteUserProfile(password: string): void {
    console.log('deleteUserProfile', password); // TODO
  }

  openImageChooserDialog(): void {
    console.log('openImageChooserDialog'); // TODO
  }
}
