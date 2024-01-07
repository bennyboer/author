import { User } from './user';

export interface UsersState {
  users: UserLookup;
}

export interface UserLookup {
  [id: string]: UserState;
}

export interface UserState {
  loading: boolean;
  errorMessage?: string;
  user?: User;
}

export const initialState: UsersState = {
  users: {},
};
