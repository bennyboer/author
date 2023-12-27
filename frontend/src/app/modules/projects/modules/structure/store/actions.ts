import { createAction, props } from '@ngrx/store';
import { StructureTree, StructureTreeNodeId } from './state';
import { StructureTreeEvent } from './remote';

export const toggleNode = createAction(
  '[Structure] Toggle Node',
  props<{
    treeId: string;
    version: number;
    nodeId: StructureTreeNodeId;
  }>(),
);
export const nodeToggled = createAction(
  '[Structure] Node Toggled',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const togglingNodeFailed = createAction(
  '[Structure] Toggling Node Failed',
  props<{ nodeId: StructureTreeNodeId; message: string }>(),
);

export const addNode = createAction(
  '[Structure] Add Node',
  props<{
    treeId: string;
    version: number;
    parentNodeId: StructureTreeNodeId;
    name: string;
  }>(),
);
export const nodeAdded = createAction(
  '[Structure] Node Added',
  props<{
    parentNodeId: StructureTreeNodeId;
  }>(),
);
export const addingNodeFailed = createAction(
  '[Structure] Adding Node Failed',
  props<{ parentNodeId: StructureTreeNodeId; message: string }>(),
);

export const removeNode = createAction(
  '[Structure] Remove Node',
  props<{
    treeId: string;
    version: number;
    nodeId: StructureTreeNodeId;
  }>(),
);
export const removedNode = createAction(
  '[Structure] Removed Node',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const removingNodeFailed = createAction(
  '[Structure] Removing Node Failed',
  props<{ nodeId: StructureTreeNodeId; message: string }>(),
);

export const renameNode = createAction(
  '[Structure] Rename Node',
  props<{
    treeId: string;
    version: number;
    nodeId: StructureTreeNodeId;
    name: string;
  }>(),
);
export const nodeRenamed = createAction(
  '[Structure] Node Renamed',
  props<{ nodeId: StructureTreeNodeId }>(),
);
export const renamingNodeFailed = createAction(
  '[Structure] Renaming Node Failed',
  props<{ nodeId: StructureTreeNodeId; message: string }>(),
);

export const swapNodes = createAction(
  '[Structure] Swap Nodes',
  props<{
    treeId: string;
    version: number;
    nodeId1: StructureTreeNodeId;
    nodeId2: StructureTreeNodeId;
  }>(),
);
export const nodesSwapped = createAction(
  '[Structure] Nodes Swapped',
  props<{ nodeId1: StructureTreeNodeId; nodeId2: StructureTreeNodeId }>(),
);
export const swappingNodesFailed = createAction(
  '[Structure] Swapping Nodes Failed',
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

export const loadTree = createAction(
  '[Structure] Load Tree',
  props<{ treeId: string }>(),
);
export const treeLoaded = createAction(
  '[Structure] Tree Loaded',
  props<{ tree: StructureTree }>(),
);
export const loadingTreeFailed = createAction(
  '[Structure] Loading Tree Failed',
  props<{ message: string }>(),
);

export const actions = {
  loadTree,
  toggleNode,
  addNode,
  removeNode,
  swapNodes,
  renameNode,
};
