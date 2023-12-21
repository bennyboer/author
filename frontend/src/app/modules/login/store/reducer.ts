import { createReducer, on } from '@ngrx/store';
import { initialState, LoginError } from './state';
import {
  loggedIn,
  login,
  loginFailed,
  loginStateLoaded,
  logout,
  redirectAfterLogin,
  redirectAfterLoginSuccess,
} from './actions';

export const reducer = createReducer(
  initialState,

  on(login, (state) => ({ ...state, error: LoginError.None, loading: true })),
  on(loggedIn, (state, { token }) => ({ ...state, token, loading: false })),
  on(loginFailed, (state, { error }) => ({
    ...state,
    error,
    loading: false,
  })),

  on(logout, (state) => ({
    ...state,
    token: undefined,
    loading: false,
  })),

  on(redirectAfterLogin, (state, { url }) => ({
    ...state,
    redirectUrlAfterLogin: url,
  })),
  on(redirectAfterLoginSuccess, (state) => ({
    ...state,
    redirectUrlAfterLogin: undefined,
  })),

  on(loginStateLoaded, (state, { token }) => ({
    ...state,
    token,
    loading: false,
  })),
);
