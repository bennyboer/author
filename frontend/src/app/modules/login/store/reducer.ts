import { createReducer, on } from '@ngrx/store';
import { initialState, LoginError } from './state';
import {
  loggedIn,
  loginFailed,
  loginStateLoaded,
  loginViaUserName,
  logout,
  redirectAfterLogin,
  redirectAfterLoginSuccess,
} from './actions';

export const reducer = createReducer(
  initialState,

  on(loginViaUserName, (state) => ({
    ...state,
    error: LoginError.None,
    loading: true,
  })),
  on(loggedIn, (state, { token, userId }) => ({
    ...state,
    token,
    userId,
    loading: false,
  })),
  on(loginFailed, (state, { error }) => ({
    ...state,
    error,
    loading: false,
  })),

  on(logout, (state) => ({
    ...state,
    token: undefined,
    userId: undefined,
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

  on(loginStateLoaded, (state, { token, userId }) => ({
    ...state,
    token,
    userId,
    loading: false,
  })),
);
