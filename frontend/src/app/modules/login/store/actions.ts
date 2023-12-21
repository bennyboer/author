import { createAction, props } from '@ngrx/store';
import { LoginError, Token } from './state';

export const login = createAction(
  '[Login] Login',
  props<{ username: string; password: string }>(),
);
export const loginSuccess = createAction(
  '[Login] Login Success',
  props<{ token: Token }>(),
);
export const loginFailure = createAction(
  '[Login] Login Failure',
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
export const logoutSuccess = createAction('[Login] Logout Success');

export const loadLoginState = createAction('[Login] Load Login State');
export const loginStateLoaded = createAction(
  '[Login] Login State Loaded',
  props<{ token?: Token }>(),
);

export const actions = {
  login,
  logout,
  redirectAfterLogin,
  loadLoginState,
};
