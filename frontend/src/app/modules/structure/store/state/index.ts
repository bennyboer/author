import { StructureTree } from './tree';
import { ROOT_ID } from './node';

export * from './tree';
export * from './node';
export { TreeMutator } from './tree-mutator';

export interface State {
  tree: StructureTree;
  errorMessage?: string;
}

export const initialState: State = {
  tree: {
    version: 0,
    nodes: {
      [ROOT_ID]: {
        id: ROOT_ID,
        name: 'Project',
        children: [],
        expanded: true,
      },
    },
    rootId: ROOT_ID,
  },
};
