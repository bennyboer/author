import { createReducer, on } from '@ngrx/store';
import { initialState, LoginError } from './state';
import {
  login,
  loginFailure,
  loginStateLoaded,
  loginSuccess,
  logout,
} from './actions';

export const reducer = createReducer(
  initialState,

  on(login, (state) => ({ ...state, error: LoginError.None, loading: true })),
  on(loginSuccess, (state, { token }) => ({ ...state, token, loading: false })),
  on(loginFailure, (state, { error }) => ({
    ...state,
    error,
    loading: false,
  })),

  on(logout, (state) => ({
    ...state,
    token: undefined,
    loading: false,
  })),

  on(loginStateLoaded, (state, { token }) => ({
    ...state,
    token,
    loading: false,
  })),
);
