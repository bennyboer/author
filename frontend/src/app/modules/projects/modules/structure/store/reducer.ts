import { createReducer, on } from '@ngrx/store';
import { initialState, Structure, StructureMutator } from './state';
import {
  addingNodeFailed,
  eventReceived,
  loadingStructureFailed,
  loadStructure,
  removingNodeFailed,
  renamingNodeFailed,
  structureLoaded,
  swappingNodesFailed,
  togglingNodeFailed,
} from './actions';
import {
  NodeAddedEvent,
  NodeRemovedEvent,
  NodeRenamedEvent,
  NodesSwappedEvent,
  NodeToggledEvent,
  StructureEvent,
  StructureEventType,
} from './remote';
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

  on(eventReceived, (state, { event }) =>
    Option.someOrNone(state.structure)
      .flatMap((structure) =>
        applyEvent(structure, event).map((structure) => ({
          ...state,
          structure,
        })),
      )
      .orElse({
        ...state,
        structure: state.structure!,
      }),
  ),

  on(togglingNodeFailed, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to toggle node ${nodeId}: ${message}`,
  })),

  on(addingNodeFailed, (state, { parentNodeId, message }) => ({
    ...state,
    errorMessage: `Failed to add node to ${parentNodeId}: ${message}`,
  })),

  on(removingNodeFailed, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to remove node ${nodeId}: ${message}`,
  })),

  on(swappingNodesFailed, (state, { nodeId1, nodeId2, message }) => ({
    ...state,
    errorMessage: `Failed to swap nodes ${nodeId1} and ${nodeId2}: ${message}`,
  })),

  on(renamingNodeFailed, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to rename node ${nodeId}: ${message}`,
  })),
);

const applyEvent = (
  structure: Structure,
  event: StructureEvent,
): Option<Structure> => {
  const structureMutator = new StructureMutator(structure);

  switch (event.type) {
    case StructureEventType.NODE_ADDED:
      const nodeAddedEvent = event as NodeAddedEvent;
      return structureMutator.addNode(
        nodeAddedEvent.parentNodeId,
        nodeAddedEvent.id,
        nodeAddedEvent.name,
      );
    case StructureEventType.NODE_REMOVED:
      const nodeRemovedEvent = event as NodeRemovedEvent;
      return structureMutator.removeNode(nodeRemovedEvent.id);
    case StructureEventType.NODE_TOGGLED:
      const nodeToggledEvent = event as NodeToggledEvent;
      return structureMutator.toggleNode(nodeToggledEvent.id);
    case StructureEventType.NODES_SWAPPED:
      const nodesSwappedEvent = event as NodesSwappedEvent;
      return structureMutator.swapNodes(
        nodesSwappedEvent.id1,
        nodesSwappedEvent.id2,
      );
    case StructureEventType.NODE_RENAMED:
      const nodeRenamedEvent = event as NodeRenamedEvent;
      return structureMutator.renameNode(
        nodeRenamedEvent.id,
        nodeRenamedEvent.name,
      );
    default:
      return Option.none();
  }
};
