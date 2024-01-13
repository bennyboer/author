import { createAction, props } from '@ngrx/store';
import { Structure, StructureNodeId } from './state';

export const toggleNode = createAction(
  '[Structure] Toggle Node',
  props<{
    structureId: string;
    version: number;
    nodeId: StructureNodeId;
  }>(),
);
export const toggleNodeSuccess = createAction(
  '[Structure] Toggle Node Success',
  props<{
    structureId: string;
    nodeId: StructureNodeId;
  }>(),
);
export const togglingNodeFailed = createAction(
  '[Structure] Toggling Node Failed',
  props<{ structureId: string; nodeId: StructureNodeId; message: string }>(),
);
export const nodeToggled = createAction(
  '[Structure] Node Toggled',
  props<{ structureId: string; nodeId: StructureNodeId }>(),
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
export const addNodeSuccess = createAction(
  '[Structure] Add Node Success',
  props<{
    structureId: string;
    parentNodeId: StructureNodeId;
  }>(),
);
export const addingNodeFailed = createAction(
  '[Structure] Adding Node Failed',
  props<{
    structureId: string;
    parentNodeId: StructureNodeId;
    message: string;
  }>(),
);
export const nodeAdded = createAction(
  '[Structure] Node Added',
  props<{
    structureId: string;
    parentNodeId: StructureNodeId;
    nodeId: string;
    name: string;
  }>(),
);

export const removeNode = createAction(
  '[Structure] Remove Node',
  props<{
    structureId: string;
    version: number;
    nodeId: StructureNodeId;
  }>(),
);
export const removeNodeSuccess = createAction(
  '[Structure] Remove Node Success',
  props<{ structureId: string; nodeId: StructureNodeId }>(),
);
export const removingNodeFailed = createAction(
  '[Structure] Removing Node Failed',
  props<{ structureId: string; nodeId: StructureNodeId; message: string }>(),
);
export const nodeRemoved = createAction(
  '[Structure] Removed Node',
  props<{ structureId: string; nodeId: StructureNodeId }>(),
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
export const renameNodeSuccess = createAction(
  '[Structure] Rename Node Success',
  props<{ structureId: string; nodeId: StructureNodeId }>(),
);
export const renamingNodeFailed = createAction(
  '[Structure] Renaming Node Failed',
  props<{ structureId: string; nodeId: StructureNodeId; message: string }>(),
);
export const nodeRenamed = createAction(
  '[Structure] Node Renamed',
  props<{ structureId: string; nodeId: StructureNodeId; name: string }>(),
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
export const swapNodesSuccess = createAction(
  '[Structure] Swap Nodes Success',
  props<{
    structureId: string;
    nodeId1: StructureNodeId;
    nodeId2: StructureNodeId;
  }>(),
);
export const swappingNodesFailed = createAction(
  '[Structure] Swapping Nodes Failed',
  props<{
    structureId: string;
    nodeId1: StructureNodeId;
    nodeId2: StructureNodeId;
    message: string;
  }>(),
);
export const nodesSwapped = createAction(
  '[Structure] Nodes Swapped',
  props<{
    structureId: string;
    nodeId1: StructureNodeId;
    nodeId2: StructureNodeId;
  }>(),
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
