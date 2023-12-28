import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RemoteTreeService } from './remote';
import { catchError, map, mergeMap, of, switchMap } from 'rxjs';
import {
  addingNodeFailed,
  addNode,
  eventReceived,
  loadingTreeFailed,
  loadTree,
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
      mergeMap(({ treeId, version, nodeId }) => {
        return this.treeService.toggleNode(treeId, version, nodeId).pipe(
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
      mergeMap(({ treeId, version, parentNodeId, name }) => {
        return this.treeService
          .addNode(treeId, version, parentNodeId, name)
          .pipe(
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
      mergeMap(({ treeId, version, nodeId }) => {
        return this.treeService.removeNode(treeId, version, nodeId).pipe(
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
      mergeMap(({ treeId, version, nodeId, name }) => {
        return this.treeService.renameNode(treeId, version, nodeId, name).pipe(
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
      mergeMap(({ treeId, version, nodeId1, nodeId2 }) => {
        return this.treeService
          .swapNodes(treeId, version, nodeId1, nodeId2)
          .pipe(
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

  subscribeToTreeEvents$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadTree),
      switchMap(({ treeId }) => this.treeService.getEvents(treeId)),
      map((event) => eventReceived({ event })),
    ),
  );

  loadTree$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadTree),
      mergeMap(({ treeId }) => {
        return this.treeService.getTree(treeId).pipe(
          map((tree) => treeLoaded({ tree })),
          catchError((error) =>
            of(loadingTreeFailed({ message: error.message })),
          ),
        );
      }),
    ),
  );

  constructor(
    private readonly actions: Actions,
    private readonly treeService: RemoteTreeService,
  ) {}
}
