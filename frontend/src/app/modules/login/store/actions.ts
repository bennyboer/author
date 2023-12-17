import { createAction, props } from '@ngrx/store';
import { Token } from './state';

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
  props<{ message: string }>(),
);

export const actions = {
  login,
};
