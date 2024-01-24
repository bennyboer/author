import { createReducer, on } from '@ngrx/store';
import { initialState, State, UserState } from './state';
import {
  changePassword,
  changePasswordSuccess,
  changingPasswordFailed,
  firstNameUpdated,
  imageUpdated,
  lastNameUpdated,
  loadingUserFailed,
  loadUser,
  mailUpdated,
  nameUpdated,
  passwordChanged,
  removeUser,
  removeUserSuccess,
  removingUserFailed,
  updateFirstName,
  updateFirstNameSuccess,
  updateImage,
  updateImageSuccess,
  updateLastName,
  updateLastNameSuccess,
  updateMail,
  updateMailSuccess,
  updateName,
  updateNameSuccess,
  updatingFirstNameFailed,
  updatingImageFailed,
  updatingLastNameFailed,
  updatingMailFailed,
  updatingNameFailed,
  userLoaded,
  userRemoved,
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

  on(updateMail, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: true,
    })),
  ),
  on(updateMailSuccess, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
    })),
  ),
  on(updatingMailFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to update mail: ${props.message}`,
    })),
  ),
  on(mailUpdated, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        version: props.version,
        mail: props.mail,
      },
    })),
  ),

  on(removeUser, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: true,
    })),
  ),
  on(removeUserSuccess, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
    })),
  ),
  on(removingUserFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to remove user: ${props.message}`,
    })),
  ),
  on(userRemoved, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        version: props.version,
      },
    })),
  ),

  on(updateImage, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: true,
    })),
  ),
  on(updateImageSuccess, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
    })),
  ),
  on(updatingImageFailed, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      loading: false,
      errorMessage: `Failed to update image: ${props.message}`,
    })),
  ),
  on(imageUpdated, (state, props) =>
    updateUserState(state, props.id, (userState) => ({
      ...userState,
      user: {
        ...userState.user!,
        version: props.version,
        imageId: props.imageId,
      },
    })),
  ),
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
