import { Token } from './token';
import { LoginError } from './error';

export { Token } from './token';
export { LoginError, LoginErrors } from './error';

export interface State {
  loading: boolean;
  error: LoginError;
  token?: Token;
  // TODO User information (name, email, etc.)
}

export const initialState: State = {
  error: LoginError.None,
  loading: true,
};
