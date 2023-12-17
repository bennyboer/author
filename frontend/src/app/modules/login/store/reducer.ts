import { createReducer, on } from '@ngrx/store';
import { initialState } from './state';
import { login, loginFailure, loginStateLoaded, loginSuccess } from './actions';

export const reducer = createReducer(
  initialState,

  on(login, (state) => ({ ...state, errorMessage: undefined })),
  on(loginSuccess, (state, { token }) => ({ ...state, token })),
  on(loginFailure, (state, { message }) => ({
    ...state,
    errorMessage: message,
  })),

  on(loginStateLoaded, (state, { token }) => ({
    ...state,
    token,
    loading: false,
  })),
);
