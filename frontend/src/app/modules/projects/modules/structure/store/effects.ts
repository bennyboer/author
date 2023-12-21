import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { TreeService } from './remote';
import { catchError, map, mergeMap, of } from 'rxjs';
import {
  addingNodeFailed,
  addNode,
  eventReceived,
  nodeAdded,
  nodeRenamed,
  nodesSwapped,
  nodeToggled,
  removedNode,
  removeNode,
  removingNodeFailed,
  renameNode,
  renamingNodeFailed,
  swapNodes,
  swappingNodesFailed,
  toggleNode,
  togglingNodeFailed,
  treeLoaded,
} from './actions';

@Injectable()
export class StructureStoreEffects {
  toggleNode$ = createEffect(() =>
    this.actions.pipe(
      ofType(toggleNode),
      mergeMap(({ nodeId }) => {
        return this.remoteStructureTreeService.toggleNode(nodeId).pipe(
          map(() => nodeToggled({ nodeId })),
          catchError((error) =>
            of(
              togglingNodeFailed({
                nodeId: error.nodeId,
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  addNode$ = createEffect(() =>
    this.actions.pipe(
      ofType(addNode),
      mergeMap(({ parentNodeId, name }) => {
        return this.remoteStructureTreeService.addNode(parentNodeId, name).pipe(
          map(() => nodeAdded({ parentNodeId })),
          catchError((error) =>
            of(
              addingNodeFailed({
                parentNodeId: error.nodeId,
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  removeNode$ = createEffect(() =>
    this.actions.pipe(
      ofType(removeNode),
      mergeMap(({ nodeId }) => {
        return this.remoteStructureTreeService.removeNode(nodeId).pipe(
          map(() => removedNode({ nodeId })),
          catchError((error) =>
            of(
              removingNodeFailed({
                nodeId: error.nodeId,
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  renameNode$ = createEffect(() =>
    this.actions.pipe(
      ofType(renameNode),
      mergeMap(({ nodeId, name }) => {
        return this.remoteStructureTreeService.renameNode(nodeId, name).pipe(
          map(() => nodeRenamed({ nodeId })),
          catchError((error) =>
            of(
              renamingNodeFailed({
                nodeId: error.nodeId,
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  swapNodes$ = createEffect(() =>
    this.actions.pipe(
      ofType(swapNodes),
      mergeMap(({ nodeId1, nodeId2 }) => {
        return this.remoteStructureTreeService.swapNodes(nodeId1, nodeId2).pipe(
          map(() => nodesSwapped({ nodeId1, nodeId2 })),
          catchError((error) =>
            of(
              swappingNodesFailed({
                nodeId1: error.nodeId1,
                nodeId2: error.nodeId2,
                message: error.message,
              }),
            ),
          ),
        );
      }),
    ),
  );

  dispatchEvent$ = createEffect(() =>
    this.remoteStructureTreeService
      .getEvents()
      .pipe(map((event) => eventReceived({ event }))),
  );

  init$ = createEffect(() =>
    this.remoteStructureTreeService
      .getTree()
      .pipe(map((tree) => treeLoaded({ tree }))),
  );

  constructor(
    private readonly actions: Actions,
    private readonly remoteStructureTreeService: TreeService,
  ) {}
}
