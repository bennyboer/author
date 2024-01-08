import { Token } from './token';
import { LoginError } from './error';

export { Token } from './token';
export { LoginError, LoginErrors } from './error';

export interface State {
  loading: boolean;
  error: LoginError;
  userId?: string;
  token?: Token;
  redirectUrlAfterLogin?: string;
}

export const initialState: State = {
  error: LoginError.None,
  loading: true,
};
