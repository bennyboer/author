import { createReducer, on } from '@ngrx/store';
import { initialState, StructureTree, TreeMutator } from './state';
import {
  addNodeFailure,
  eventReceived,
  removeNodeFailure,
  renameNodeFailure,
  swapNodesFailure,
  toggleNodeFailure,
  treeLoaded,
} from './actions';
import {
  NodeAddedEvent,
  NodeRemovedEvent,
  NodeRenamedEvent,
  NodesSwappedEvent,
  NodeToggledEvent,
  StructureTreeEvent,
  StructureTreeEventType,
} from './remote';
import { Option } from '../../shared';

export const reducer = createReducer(
  initialState,

  on(treeLoaded, (state, { tree }) => ({ ...state, tree })),

  on(eventReceived, (state, { event }) =>
    Option.someOrNone(state.tree)
      .flatMap((tree) =>
        applyEvent(tree, event).map((tree) => ({ ...state, tree })),
      )
      .orElse({
        ...state,
        tree: state.tree!,
      }),
  ),

  on(toggleNodeFailure, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to toggle node ${nodeId}: ${message}`,
  })),

  on(addNodeFailure, (state, { parentNodeId, message }) => ({
    ...state,
    errorMessage: `Failed to add node to ${parentNodeId}: ${message}`,
  })),

  on(removeNodeFailure, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to remove node ${nodeId}: ${message}`,
  })),

  on(swapNodesFailure, (state, { nodeId1, nodeId2, message }) => ({
    ...state,
    errorMessage: `Failed to swap nodes ${nodeId1} and ${nodeId2}: ${message}`,
  })),

  on(renameNodeFailure, (state, { nodeId, message }) => ({
    ...state,
    errorMessage: `Failed to rename node ${nodeId}: ${message}`,
  })),
);

const applyEvent = (
  tree: StructureTree,
  event: StructureTreeEvent,
): Option<StructureTree> => {
  const treeMutator = new TreeMutator(tree);

  switch (event.type) {
    case StructureTreeEventType.NODE_ADDED:
      const nodeAddedEvent = event as NodeAddedEvent;
      return treeMutator.addNode(
        nodeAddedEvent.parentNodeId,
        nodeAddedEvent.id,
        nodeAddedEvent.name,
      );
    case StructureTreeEventType.NODE_REMOVED:
      const nodeRemovedEvent = event as NodeRemovedEvent;
      return treeMutator.removeNode(nodeRemovedEvent.id);
    case StructureTreeEventType.NODE_TOGGLED:
      const nodeToggledEvent = event as NodeToggledEvent;
      return treeMutator.toggleNode(nodeToggledEvent.id);
    case StructureTreeEventType.NODES_SWAPPED:
      const nodesSwappedEvent = event as NodesSwappedEvent;
      return treeMutator.swapNodes(
        nodesSwappedEvent.id1,
        nodesSwappedEvent.id2,
      );
    case StructureTreeEventType.NODE_RENAMED:
      const nodeRenamedEvent = event as NodeRenamedEvent;
      return treeMutator.renameNode(nodeRenamedEvent.id, nodeRenamedEvent.name);
    default:
      return Option.none();
  }
};
