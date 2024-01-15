import { createReducer, on } from '@ngrx/store';
import { initialState, State, UserState } from './state';
import {
  loadingUserFailed,
  loadUser,
  nameUpdated,
  updateName,
  updateNameSuccess,
  updatingNameFailed,
  userLoaded,
} from './actions';

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
  on(userLoaded, (state, props) =>
    updateUserState(state, props.user.id, (userState) => ({
      ...userState,
      loading: false,
      user: props.user,
    })),
  ),
  on(loadingUserFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to load user: ${props.message}`,
    })),
  ),

  on(updateName, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: true,
    })),
  ),
  on(updateNameSuccess, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
    })),
  ),
  on(updatingNameFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to update name: ${props.message}`,
    })),
  ),
  on(nameUpdated, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        name: props.name,
      },
    })),
  ),

  // TODO: Add reducers for other actions.
);

const updateUserState = (
  state: State,
  userId: string,
  updater: (userState: UserState) => UserState,
) => {
  const userState = state.users[userId];
  if (!userState) {
    return state;
  }

  return {
    ...state,
    users: {
      ...state.users,
      [userId]: updater(userState),
    },
  };
};
