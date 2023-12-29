import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { RemoteStructureService } from './remote';
import { catchError, map, mergeMap, of, switchMap } from 'rxjs';
import {
  addingNodeFailed,
  addNode,
  eventReceived,
  loadingStructureFailed,
  loadStructure,
  nodeAdded,
  nodeRenamed,
  nodesSwapped,
  nodeToggled,
  removedNode,
  removeNode,
  removingNodeFailed,
  renameNode,
  renamingNodeFailed,
  structureLoaded,
  swapNodes,
  swappingNodesFailed,
  toggleNode,
  togglingNodeFailed,
} from './actions';

@Injectable()
export class StructureStoreEffects {
  toggleNode$ = createEffect(() =>
    this.actions.pipe(
      ofType(toggleNode),
      mergeMap(({ structureId, version, nodeId }) => {
        return this.structureService
          .toggleNode(structureId, version, nodeId)
          .pipe(
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
      mergeMap(({ structureId, version, parentNodeId, name }) => {
        return this.structureService
          .addNode(structureId, version, parentNodeId, name)
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
      mergeMap(({ structureId, version, nodeId }) => {
        return this.structureService
          .removeNode(structureId, version, nodeId)
          .pipe(
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
      mergeMap(({ structureId, version, nodeId, name }) => {
        return this.structureService
          .renameNode(structureId, version, nodeId, name)
          .pipe(
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
      mergeMap(({ structureId, version, nodeId1, nodeId2 }) => {
        return this.structureService
          .swapNodes(structureId, version, nodeId1, nodeId2)
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

  subscribeToStructureEvents$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadStructure),
      switchMap(({ structureId }) =>
        this.structureService.getEvents(structureId),
      ),
      map((event) => eventReceived({ event })),
    ),
  );

  loadStructure$ = createEffect(() =>
    this.actions.pipe(
      ofType(loadStructure),
      mergeMap(({ structureId }) => {
        return this.structureService.getStructure(structureId).pipe(
          map((structure) => structureLoaded({ structure })),
          catchError((error) =>
            of(loadingStructureFailed({ message: error.message })),
          ),
        );
      }),
    ),
  );

  constructor(
    private readonly actions: Actions,
    private readonly structureService: RemoteStructureService,
  ) {}
}
