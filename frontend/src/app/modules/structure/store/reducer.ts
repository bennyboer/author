import { createReducer, on } from '@ngrx/store';
import { initialState, StructureTree, TreeMutator } from './state';
import {
  addNodeFailure,
  eventReceived,
  removeNodeFailure,
  swapNodesFailure,
  toggleNodeFailure,
  treeLoaded,
} from './actions';
import {
  EventType,
  NodeAddedEvent,
  NodeRemovedEvent,
  NodesSwappedEvent,
  NodeToggledEvent,
  StructureTreeEvent,
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
);

const applyEvent = (
  tree: StructureTree,
  event: StructureTreeEvent,
): Option<StructureTree> => {
  const treeMutator = new TreeMutator(tree);

  switch (event.type) {
    case EventType.NODE_ADDED:
      const nodeAddedEvent = event as NodeAddedEvent;
      return treeMutator.addNode(
        nodeAddedEvent.parentNodeId,
        nodeAddedEvent.id,
        nodeAddedEvent.name,
      );
    case EventType.NODE_REMOVED:
      const nodeRemovedEvent = event as NodeRemovedEvent;
      return treeMutator.removeNode(nodeRemovedEvent.id);
    case EventType.NODE_TOGGLED:
      const nodeToggledEvent = event as NodeToggledEvent;
      return treeMutator.toggleNode(nodeToggledEvent.id);
    case EventType.NODES_SWAPPED:
      const nodesSwappedEvent = event as NodesSwappedEvent;
      return treeMutator.swapNodes(
        nodesSwappedEvent.id1,
        nodesSwappedEvent.id2,
      );
    default:
      return Option.none();
  }
};
