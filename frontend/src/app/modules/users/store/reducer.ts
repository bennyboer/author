import { createReducer, on } from '@ngrx/store';
import { initialState, State, UserState } from './state';
import {
  changePassword,
  changePasswordSuccess,
  changingPasswordFailed,
  firstNameUpdated,
  lastNameUpdated,
  loadingUserFailed,
  loadUser,
  nameUpdated,
  passwordChanged,
  updateFirstName,
  updateFirstNameSuccess,
  updateLastName,
  updateLastNameSuccess,
  updateName,
  updateNameSuccess,
  updatingFirstNameFailed,
  updatingLastNameFailed,
  updatingNameFailed,
  userLoaded,
  versionUpdated,
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

  on(versionUpdated, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        version: props.version,
      },
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
        version: props.version,
        name: props.name,
      },
    })),
  ),

  on(updateFirstName, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: true,
    })),
  ),
  on(updateFirstNameSuccess, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
    })),
  ),
  on(updatingFirstNameFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to update first name: ${props.message}`,
    })),
  ),
  on(firstNameUpdated, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        version: props.version,
        firstName: props.firstName,
      },
    })),
  ),

  on(updateLastName, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: true,
    })),
  ),
  on(updateLastNameSuccess, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
    })),
  ),
  on(updatingLastNameFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to update last name: ${props.message}`,
    })),
  ),
  on(lastNameUpdated, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        version: props.version,
        lastName: props.lastName,
      },
    })),
  ),

  on(changePassword, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: true,
    })),
  ),
  on(changePasswordSuccess, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
    })),
  ),
  on(changingPasswordFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to change password: ${props.message}`,
    })),
  ),
  on(passwordChanged, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        version: props.version,
        password: props.password,
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
