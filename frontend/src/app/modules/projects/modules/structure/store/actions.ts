import { createAction, props } from '@ngrx/store';
import { StructureTree, StructureTreeNodeId } from './state';
import { StructureTreeEvent } from './remote';

export const toggleNode = createAction(
  '[Structure] Toggle Node',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const toggleNodeSuccess = createAction(
  '[Structure] Toggle Node Success',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const toggleNodeFailure = createAction(
  '[Structure] Toggle Node Failure',
  props<{ nodeId: StructureTreeNodeId; message: string }>(),
);

export const addNode = createAction(
  '[Structure] Add Node',
  props<{ parentNodeId: StructureTreeNodeId; name: string }>(),
);
export const addNodeSuccess = createAction(
  '[Structure] Add Node Success',
  props<{
    parentNodeId: StructureTreeNodeId;
  }>(),
);
export const addNodeFailure = createAction(
  '[Structure] Add Node Failure',
  props<{ parentNodeId: StructureTreeNodeId; message: string }>(),
);

export const removeNode = createAction(
  '[Structure] Remove Node',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const removeNodeSuccess = createAction(
  '[Structure] Remove Node Success',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const removeNodeFailure = createAction(
  '[Structure] Remove Node Failure',
  props<{ nodeId: StructureTreeNodeId; message: string }>(),
);

export const renameNode = createAction(
  '[Structure] Rename Node',
  props<{ nodeId: StructureTreeNodeId; name: string }>(),
);
export const renameNodeSuccess = createAction(
  '[Structure] Rename Node Success',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const renameNodeFailure = createAction(
  '[Structure] Rename Node Failure',
  props<{ nodeId: StructureTreeNodeId; message: string }>(),
);

export const swapNodes = createAction(
  '[Structure] Swap Nodes',
  props<{ nodeId1: StructureTreeNodeId; nodeId2: StructureTreeNodeId }>(),
);
export const swapNodesSuccess = createAction(
  '[Structure] Swap Nodes Success',
  props<{ nodeId1: StructureTreeNodeId; nodeId2: StructureTreeNodeId }>(),
);
export const swapNodesFailure = createAction(
  '[Structure] Swap Nodes Failure',
  props<{
    nodeId1: StructureTreeNodeId;
    nodeId2: StructureTreeNodeId;
    message: string;
  }>(),
);

export const eventReceived = createAction(
  '[Structure] Event Received',
  props<{ event: StructureTreeEvent }>(),
);

export const treeLoaded = createAction(
  '[Structure] Tree Loaded',
  props<{ tree: StructureTree }>(),
);

export const actions = {
  toggleNode,
  addNode,
  removeNode,
  swapNodes,
  renameNode,
};
