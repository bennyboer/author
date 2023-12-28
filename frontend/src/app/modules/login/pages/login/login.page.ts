import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { LoginService } from '../../store';
import { Option } from '../../../shared';
import { filter, first, map, Observable, race, Subject, takeUntil } from 'rxjs';
import { LoginError } from '../../store/state';

@Component({
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage implements OnDestroy {
  readonly LoginError = LoginError;

  readonly formGroup = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
  });

  readonly loading$: Observable<boolean> = this.loginService.isLoading();

  readonly error$: Observable<LoginError> = this.loginService.getError();

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(private readonly loginService: LoginService) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  login(): void {
    this.formGroup.disable();

    const username = Option.someOrNone(
      this.formGroup.value.username,
    ).orElseThrow();
    const password = Option.someOrNone(
      this.formGroup.value.password,
    ).orElseThrow();

    this.loginService.login(username, password);

    const loggedIn$ = this.loginService.isLoggedIn().pipe(filter((b) => b));
    const loginError$ = this.loginService
      .getError()
      .pipe(filter((e) => e !== LoginError.None));

    race([loggedIn$, loginError$])
      .pipe(first(), takeUntil(this.destroy$))
      .subscribe(() => this.formGroup.enable());
  }

  isNotLoading(): Observable<boolean> {
    return this.loading$.pipe(map((loading) => !loading));
  }
}
