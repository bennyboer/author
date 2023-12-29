import { createAction, props } from '@ngrx/store';
import { Structure, StructureNodeId } from './state';
import { StructureEvent } from './remote';

export const toggleNode = createAction(
  '[Structure] Toggle Node',
  props<{
    structureId: string;
    version: number;
    nodeId: StructureNodeId;
  }>(),
);
export const nodeToggled = createAction(
  '[Structure] Node Toggled',
  props<{ nodeId: StructureNodeId }>(),
);
export const togglingNodeFailed = createAction(
  '[Structure] Toggling Node Failed',
  props<{ nodeId: StructureNodeId; message: string }>(),
);

export const addNode = createAction(
  '[Structure] Add Node',
  props<{
    structureId: string;
    version: number;
    parentNodeId: StructureNodeId;
    name: string;
  }>(),
);
export const nodeAdded = createAction(
  '[Structure] Node Added',
  props<{
    parentNodeId: StructureNodeId;
  }>(),
);
export const addingNodeFailed = createAction(
  '[Structure] Adding Node Failed',
  props<{ parentNodeId: StructureNodeId; message: string }>(),
);

export const removeNode = createAction(
  '[Structure] Remove Node',
  props<{
    structureId: string;
    version: number;
    nodeId: StructureNodeId;
  }>(),
);
export const removedNode = createAction(
  '[Structure] Removed Node',
  props<{ nodeId: StructureNodeId }>(),
);
export const removingNodeFailed = createAction(
  '[Structure] Removing Node Failed',
  props<{ nodeId: StructureNodeId; message: string }>(),
);

export const renameNode = createAction(
  '[Structure] Rename Node',
  props<{
    structureId: string;
    version: number;
    nodeId: StructureNodeId;
    name: string;
  }>(),
);
export const nodeRenamed = createAction(
  '[Structure] Node Renamed',
  props<{ nodeId: StructureNodeId }>(),
);
export const renamingNodeFailed = createAction(
  '[Structure] Renaming Node Failed',
  props<{ nodeId: StructureNodeId; message: string }>(),
);

export const swapNodes = createAction(
  '[Structure] Swap Nodes',
  props<{
    structureId: string;
    version: number;
    nodeId1: StructureNodeId;
    nodeId2: StructureNodeId;
  }>(),
);
export const nodesSwapped = createAction(
  '[Structure] Nodes Swapped',
  props<{ nodeId1: StructureNodeId; nodeId2: StructureNodeId }>(),
);
export const swappingNodesFailed = createAction(
  '[Structure] Swapping Nodes Failed',
  props<{
    nodeId1: StructureNodeId;
    nodeId2: StructureNodeId;
    message: string;
  }>(),
);

export const eventReceived = createAction(
  '[Structure] Event Received',
  props<{ event: StructureEvent }>(),
);

export const loadStructure = createAction(
  '[Structure] Load Structure',
  props<{ structureId: string }>(),
);
export const structureLoaded = createAction(
  '[Structure] Structure Loaded',
  props<{ structure: Structure }>(),
);
export const loadingStructureFailed = createAction(
  '[Structure] Loading Structure Failed',
  props<{ message: string }>(),
);

export const actions = {
  loadStructure,
  toggleNode,
  addNode,
  removeNode,
  swapNodes,
  renameNode,
};
