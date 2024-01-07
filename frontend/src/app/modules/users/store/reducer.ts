import { createReducer, on } from '@ngrx/store';
import { initialState, UserState } from './state';
import { loadingUserFailed, loadUser, userLoaded } from './actions';

export const reducer = createReducer(
  initialState,

  on(loadUser, (state, props) => ({
    ...state,
    users: {
      ...state.users,
      [props.id]: {
        loading: true,
      } as UserState,
    },
  })),
  on(userLoaded, (state, props) => ({
    ...state,
    users: {
      ...state.users,
      [props.user.id]: {
        loading: false,
        user: props.user,
      } as UserState,
    },
  })),
  on(loadingUserFailed, (state, props) => ({
    ...state,
    users: {
      ...state.users,
      [props.id]: {
        loading: false,
        errorMessage: `Failed to load user: ${props.message}`,
      } as UserState,
    },
  })),

  // TODO: Add reducers for other actions.
);
