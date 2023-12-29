import { Structure } from './structure';

export * from './structure';
export * from './node';
export { StructureMutator } from './structure-mutator';

export interface State {
  loading: boolean;
  structure?: Structure;
  errorMessage?: string;
}

export const initialState: State = {
  loading: true,
};
