import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { LoginService } from '../../store';
import { Option } from '../../../shared';
import { map, Observable } from 'rxjs';
import { LoginError } from '../../store/state';

@Component({
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  readonly LoginError = LoginError;

  readonly formGroup = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
  });

  readonly loading$: Observable<boolean> = this.loginService.isLoading();

  readonly error$: Observable<LoginError> = this.loginService.getError();

  constructor(private readonly loginService: LoginService) {}

  login(): void {
    const username = Option.someOrNone(
      this.formGroup.value.username,
    ).orElseThrow();
    const password = Option.someOrNone(
      this.formGroup.value.password,
    ).orElseThrow();

    this.loginService.login(username, password);
  }

  isNotLoading(): Observable<boolean> {
    return this.loading$.pipe(map((loading) => !loading));
  }
}
