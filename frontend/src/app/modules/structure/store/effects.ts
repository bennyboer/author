import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RemoteStructureTreeService } from './remote';
import { catchError, map, mergeMap, of } from 'rxjs';
import {
  addNode,
  addNodeFailure,
  addNodeSuccess,
  eventReceived,
  removeNode,
  removeNodeFailure,
  removeNodeSuccess,
  renameNode,
  renameNodeFailure,
  renameNodeSuccess,
  swapNodes,
  swapNodesFailure,
  swapNodesSuccess,
  toggleNode,
  toggleNodeFailure,
  toggleNodeSuccess,
  treeLoaded,
} from './actions';

@Injectable()
export class StructureStoreEffects {
  toggleNode$ = createEffect(() =>
    this.actions.pipe(
      ofType(toggleNode),
      mergeMap(({ nodeId }) => {
        return this.remoteStructureTreeService.toggleNode(nodeId).pipe(
          map(() => toggleNodeSuccess({ nodeId })),
          catchError((error) =>
            of(
              toggleNodeFailure({
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
          map(() => addNodeSuccess({ parentNodeId })),
          catchError((error) =>
            of(
              addNodeFailure({
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
          map(() => removeNodeSuccess({ nodeId })),
          catchError((error) =>
            of(
              removeNodeFailure({
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
          map(() => renameNodeSuccess({ nodeId })),
          catchError((error) =>
            of(
              renameNodeFailure({
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
          map(() => swapNodesSuccess({ nodeId1, nodeId2 })),
          catchError((error) =>
            of(
              swapNodesFailure({
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
    private readonly remoteStructureTreeService: RemoteStructureTreeService,
  ) {}
}
