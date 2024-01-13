import { createReducer, on } from '@ngrx/store';
import { initialState, State, Structure, StructureMutator } from './state';
import {
  addingNodeFailed,
  loadingStructureFailed,
  loadStructure,
  nodeAdded,
  nodeRemoved,
  nodeRenamed,
  nodesSwapped,
  nodeToggled,
  removingNodeFailed,
  renamingNodeFailed,
  structureLoaded,
  swappingNodesFailed,
  togglingNodeFailed,
} from './actions';
import { Option } from '../../../../shared';

export const reducer = createReducer(
  initialState,

  on(loadStructure, (state) => ({
    ...state,
    structure: undefined,
    loading: true,
  })),
  on(structureLoaded, (state, { structure }) => ({
    ...state,
    structure,
    loading: false,
  })),
  on(loadingStructureFailed, (state, { message }) => ({
    ...state,
    errorMessage: `Failed to load structure: ${message}`,
  })),

  on(nodeToggled, (state, { nodeId }) =>
    updateStructure(state, (mutator) => mutator.toggleNode(nodeId)),
  ),
  on(togglingNodeFailed, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to toggle node ${nodeId}: ${message}`,
  })),

  on(nodeAdded, (state, { parentNodeId, nodeId, name }) =>
    updateStructure(state, (mutator) =>
      mutator.addNode(parentNodeId, nodeId, name),
    ),
  ),
  on(addingNodeFailed, (state, { parentNodeId, message }) => ({
    ...state,
    errorMessage: `Failed to add node to ${parentNodeId}: ${message}`,
  })),

  on(nodeRemoved, (state, { nodeId }) =>
    updateStructure(state, (mutator) => mutator.removeNode(nodeId)),
  ),
  on(removingNodeFailed, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to remove node ${nodeId}: ${message}`,
  })),

  on(nodesSwapped, (state, { nodeId1, nodeId2 }) =>
    updateStructure(state, (mutator) => mutator.swapNodes(nodeId1, nodeId2)),
  ),
  on(swappingNodesFailed, (state, { nodeId1, nodeId2, message }) => ({
    ...state,
    errorMessage: `Failed to swap nodes ${nodeId1} and ${nodeId2}: ${message}`,
  })),

  on(nodeRenamed, (state, { nodeId, name }) =>
    updateStructure(state, (mutator) => mutator.renameNode(nodeId, name)),
  ),
  on(renamingNodeFailed, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to rename node ${nodeId}: ${message}`,
  })),
);

const updateStructure = (
  state: State,
  updater: (mutator: StructureMutator) => Option<Structure>,
) =>
  Option.someOrNone(state.structure)
    .flatMap((structure) => updater(new StructureMutator(structure)))
    .map((structure) => ({
      ...state,
      structure,
    }))
    .orElse({
      ...state,
      structure: state.structure!,
    });
