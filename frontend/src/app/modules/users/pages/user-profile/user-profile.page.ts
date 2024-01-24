import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { NavigationService } from '../../../../services';
import { ActivatedRoute } from '@angular/router';
import { EditRequest, Option } from '../../../shared';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { UsersService } from '../../store';
import {
  filter,
  first,
  map,
  Observable,
  race,
  ReplaySubject,
  skip,
  Subject,
  switchMap,
  takeUntil,
  tap,
} from 'rxjs';
import { User } from '../../models';
import { MatDialog } from '@angular/material/dialog';
import { ImageChooserDialog } from '../../dialogs';

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

  private userId!: string;
  private userVersion!: number;
  private readonly user$: Subject<User> = new ReplaySubject<User>(1);
  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly Validators = Validators;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog,
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
        tap((user) => {
          this.userId = user.id;
          this.userVersion = user.version;
        }),
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
    return this.user$.pipe(map((user) => user.password));
  }

  getFirstName(): Observable<string> {
    return this.user$.pipe(map((user) => user.firstName));
  }

  getLastName(): Observable<string> {
    return this.user$.pipe(map((user) => user.lastName));
  }

  updateUserName(request: EditRequest): void {
    this.usersService.updateUserName(
      this.userId,
      this.userVersion,
      request.newValue,
    );

    this.checkUserUpdateSuccess({
      request,
      success: this.getUserName().pipe(
        filter((name) => name === request.newValue),
      ),
    });
  }

  updateMail(request: EditRequest): void {
    this.usersService.updateMail(
      this.userId,
      this.userVersion,
      request.newValue,
    );

    this.checkUserUpdateSuccess({
      request,
      success: this.getMail().pipe(filter((mail) => mail === request.newValue)),
    });
  }

  updatePassword(request: EditRequest): void {
    this.usersService.changePassword(
      this.userId,
      this.userVersion,
      request.newValue,
    );

    this.checkUserUpdateSuccess({
      request,
      success: this.getPassword().pipe(
        skip(1), // Skip the initial value since it is the same as the old value
      ),
    });
  }

  updateFirstName(request: EditRequest): void {
    this.usersService.updateFirstName(
      this.userId,
      this.userVersion,
      request.newValue,
    );

    this.checkUserUpdateSuccess({
      request,
      success: this.getFirstName().pipe(
        filter((firstName) => firstName === request.newValue),
      ),
    });
  }

  updateLastName(request: EditRequest): void {
    this.usersService.updateLastName(
      this.userId,
      this.userVersion,
      request.newValue,
    );

    this.checkUserUpdateSuccess({
      request,
      success: this.getLastName().pipe(
        filter((lastName) => lastName === request.newValue),
      ),
    });
  }

  private updateImage(imageId: string): void {
    this.usersService.updateImage(this.userId, this.userVersion, imageId);
  }

  deleteUserProfile(password: string): void {
    // TODO Password is currently only used for confirmation - we don't actually check it - might be good it we did
    this.usersService.removeUser(this.userId, this.userVersion);
  }

  openImageChooserDialog(): void {
    this.dialog.open(ImageChooserDialog, {
      data: {
        userId: this.userId,
        version: this.userVersion,
      },
    });
  }

  private checkUserUpdateSuccess(props: {
    request: EditRequest;
    success: Observable<unknown>;
  }): void {
    const { request, success } = props;

    const success$ = success.pipe(tap(() => request.approve()));
    const failure$ = this.usersService.isError(this.userId).pipe(
      filter((isError) => isError),
      tap(() => request.reject()),
    );

    race([success$, failure$])
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe();
  }
}
