import { StructureTree } from './tree';

export * from './tree';
export * from './node';
export { TreeMutator } from './tree-mutator';

export interface State {
  tree?: StructureTree | undefined;
  errorMessage?: string;
}

export const initialState: State = {};