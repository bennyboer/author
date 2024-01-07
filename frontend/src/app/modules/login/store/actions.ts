import { createAction, props } from '@ngrx/store';
import { LoginError, Token } from './state';

export const loginViaUserName = createAction(
  '[Login] Login via user name',
  props<{ username: string; password: string }>(),
);
export const loginViaMail = createAction(
  '[Login] Login via mail',
  props<{ mail: string; password: string }>(),
);
export const loggedIn = createAction(
  '[Login] Logged In',
  props<{ token: Token }>(),
);
export const loginFailed = createAction(
  '[Login] Login Failed',
  props<{ error: LoginError }>(),
);

export const redirectAfterLogin = createAction(
  '[Login] Redirect After Login',
  props<{ url: string }>(),
);
export const redirectAfterLoginSuccess = createAction(
  '[Login] Redirect After Login Success',
);

export const logout = createAction('[Login] Logout');
export const loggedOut = createAction('[Login] Logged Out');

export const loadLoginState = createAction('[Login] Load Login State');
export const loginStateLoaded = createAction(
  '[Login] Login State Loaded',
  props<{ token?: Token }>(),
);

export const actions = {
  loginViaUserName,
  loginViaMail,
  logout,
  redirectAfterLogin,
  loadLoginState,
};
