import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { LoginService } from '../../store';
import { Option } from '../../../shared';

@Component({
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage {
  formGroup = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
  });

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
}
