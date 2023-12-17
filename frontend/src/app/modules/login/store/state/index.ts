import { Token } from './token';

export { Token } from './token';

export interface State {
  loading: boolean;
  errorMessage?: string;
  token?: Token;
  // TODO User information (name, email, etc.)
}

export const initialState: State = {
  loading: true,
};
