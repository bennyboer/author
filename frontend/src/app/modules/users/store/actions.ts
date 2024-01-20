import { createAction, props } from '@ngrx/store';
import { User } from './state/user';

export const loadUser = createAction(
  '[User] Load User',
  props<{ id: string }>(),
);
export const userLoaded = createAction(
  '[User] User Loaded',
  props<{ user: User }>(),
);
export const loadingUserFailed = createAction(
  '[User] User Load Error',
  props<{ id: string; message: string }>(),
);

export const updateName = createAction(
  '[User] Update Name',
  props<{ id: string; version: number; name: string }>(),
);
export const updateNameSuccess = createAction(
  '[User] Update Name Success',
  props<{ id: string }>(),
);
export const updatingNameFailed = createAction(
  '[User] Name Update Error',
  props<{ id: string; message: string }>(),
);
export const nameUpdated = createAction(
  '[User] Name Updated',
  props<{ id: string; version: number; name: string }>(),
);

export const updateFirstName = createAction(
  '[User] Update First Name',
  props<{ id: string; version: number; firstName: string }>(),
);
export const updateFirstNameSuccess = createAction(
  '[User] Update First Name Success',
  props<{ id: string }>(),
);
export const updatingFirstNameFailed = createAction(
  '[User] First Name Update Error',
  props<{ id: string; message: string }>(),
);
export const firstNameUpdated = createAction(
  '[User] First Name Updated',
  props<{ id: string; version: number; firstName: string }>(),
);

export const updateLastName = createAction(
  '[User] Update Last Name',
  props<{ id: string; version: number; lastName: string }>(),
);
export const updateLastNameSuccess = createAction(
  '[User] Update Last Name Success',
  props<{ id: string }>(),
);
export const updatingLastNameFailed = createAction(
  '[User] Last Name Update Error',
  props<{ id: string; message: string }>(),
);
export const lastNameUpdated = createAction(
  '[User] Last Name Updated',
  props<{ id: string; version: number; lastName: string }>(),
);

export const changePassword = createAction(
  '[User] Change Password',
  props<{ id: string; version: number; password: string }>(),
);
export const changePasswordSuccess = createAction(
  '[User] Change Password Success',
  props<{ id: string }>(),
);
export const changingPasswordFailed = createAction(
  '[User] Password Change Error',
  props<{ id: string; message: string }>(),
);
export const passwordChanged = createAction(
  '[User] Password Changed',
  props<{ id: string; version: number; password: string }>(),
);

export const updateMail = createAction(
  '[User] Update Mail',
  props<{ id: string; version: number; mail: string }>(),
);
export const updateMailSuccess = createAction(
  '[User] Update Mail Success',
  props<{ id: string }>(),
);
export const updatingMailFailed = createAction(
  '[User] Mail Update Error',
  props<{ id: string; message: string }>(),
);
export const mailUpdated = createAction(
  '[User] Mail Updated',
  props<{ id: string; version: number; mail: string }>(),
);

export const updateImage = createAction(
  '[User] Update Image',
  props<{ id: string; version: number; image: File }>(),
);
export const updateImageSuccess = createAction(
  '[User] Update Image Success',
  props<{ id: string }>(),
);
export const updatingImageFailed = createAction(
  '[User] Image Update Error',
  props<{ id: string; message: string }>(),
);
export const imageUpdated = createAction(
  '[User] Image Updated',
  props<{ id: string; version: number; imageId: string }>(),
);

export const versionUpdated = createAction(
  '[User] Version updated',
  props<{ id: string; version: number }>(),
);

export const actions = {
  loadUser,
  updateName,
  updateFirstName,
  updateLastName,
  changePassword,
};
