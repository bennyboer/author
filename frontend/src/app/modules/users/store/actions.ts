import { createAction, props } from '@ngrx/store';
import { User } from './state/user';
import { UserEvent } from './remote';

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

export const updateImage = createAction(
  '[User] Update Image',
  props<{ id: string; image: File }>(),
);
export const imageUpdated = createAction(
  '[User] Image Updated',
  props<{ id: string; imageId: string }>(),
);
export const updatingImageFailed = createAction(
  '[User] Image Update Error',
  props<{ id: string; message: string }>(),
);

export const updateName = createAction(
  '[User] Update Name',
  props<{ id: string; name: string }>(),
);
export const nameUpdated = createAction(
  '[User] Name Updated',
  props<{ id: string; name: string }>(),
);
export const updatingNameFailed = createAction(
  '[User] Name Update Error',
  props<{ id: string; message: string }>(),
);

export const updateMail = createAction(
  '[User] Update Mail',
  props<{ id: string; mail: string }>(),
);
export const mailUpdated = createAction(
  '[User] Mail Updated',
  props<{ id: string; mail: string }>(),
);
export const updatingMailFailed = createAction(
  '[User] Mail Update Error',
  props<{ id: string; message: string }>(),
);

export const rename = createAction(
  '[User] Rename',
  props<{ id: string; firstName: string; lastName: string }>(),
);
export const renamed = createAction('[User] Renamed', props<{ id: string }>());
export const renamingFailed = createAction(
  '[User] Renaming Error',
  props<{ id: string; message: string }>(),
);

export const updatePassword = createAction(
  '[User] Update Password',
  props<{ id: string; password: string }>(),
);
export const passwordUpdated = createAction(
  '[User] Password Updated',
  props<{ id: string }>(),
);
export const updatingPasswordFailed = createAction(
  '[User] Password Update Error',
  props<{ id: string; message: string }>(),
);

export const eventReceived = createAction(
  '[User] Event Received',
  props<{ event: UserEvent }>(),
);

export const actions = {
  loadUser,
};
