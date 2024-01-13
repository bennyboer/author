import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import {
  NodeAddedEvent,
  NodeRemovedEvent,
  NodeRenamedEvent,
  NodesSwappedEvent,
  NodeToggledEvent,
  RemoteStructureService,
  StructureEventType,
} from './remote';
import { catchError, map, mergeMap, of, switchMap } from 'rxjs';
import {
  addingNodeFailed,
  addNode,
  addNodeSuccess,
  loadingStructureFailed,
  loadStructure,
  nodeAdded,
  nodeRemoved,
  nodeRenamed,
  nodesSwapped,
  nodeToggled,
  removeNode,
  removeNodeSuccess,
  removingNodeFailed,
  renameNode,
  renameNodeSuccess,
  renamingNodeFailed,
  snapshotted,
  structureLoaded,
  swapNodes,
  swapNodesSuccess,
  swappingNodesFailed,
  toggleNode,
  toggleNodeSuccess,
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
            map(() => toggleNodeSuccess({ structureId, nodeId })),
            catchError((error) =>
              of(
                togglingNodeFailed({
                  structureId: structureId,
                  nodeId: nodeId,
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
            map(() => addNodeSuccess({ structureId, parentNodeId })),
            catchError((error) =>
              of(
                addingNodeFailed({
                  structureId: structureId,
                  parentNodeId: parentNodeId,
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
            map(() => removeNodeSuccess({ structureId, nodeId })),
            catchError((error) =>
              of(
                removingNodeFailed({
                  structureId: structureId,
                  nodeId: nodeId,
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
            map(() => renameNodeSuccess({ structureId, nodeId })),
            catchError((error) =>
              of(
                renamingNodeFailed({
                  structureId: structureId,
                  nodeId: nodeId,
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
            map(() => swapNodesSuccess({ structureId, nodeId1, nodeId2 })),
            catchError((error) =>
              of(
                swappingNodesFailed({
                  structureId: structureId,
                  nodeId1: nodeId1,
                  nodeId2: nodeId2,
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
      map((event) => {
        switch (event.type) {
          case StructureEventType.NODE_ADDED:
            const nodeAddedEvent = event as NodeAddedEvent;
            return nodeAdded({
              structureId: nodeAddedEvent.structureId,
              version: nodeAddedEvent.version,
              parentNodeId: nodeAddedEvent.parentNodeId,
              nodeId: nodeAddedEvent.id,
              name: nodeAddedEvent.name,
            });
          case StructureEventType.NODE_REMOVED:
            const nodeRemovedEvent = event as NodeRemovedEvent;
            return nodeRemoved({
              structureId: nodeRemovedEvent.structureId,
              version: nodeRemovedEvent.version,
              nodeId: nodeRemovedEvent.id,
            });
          case StructureEventType.NODE_TOGGLED:
            const nodeToggledEvent = event as NodeToggledEvent;
            return nodeToggled({
              structureId: nodeToggledEvent.structureId,
              version: nodeToggledEvent.version,
              nodeId: nodeToggledEvent.id,
            });
          case StructureEventType.NODES_SWAPPED:
            const nodesSwappedEvent = event as NodesSwappedEvent;
            return nodesSwapped({
              structureId: nodesSwappedEvent.structureId,
              version: nodesSwappedEvent.version,
              nodeId1: nodesSwappedEvent.id1,
              nodeId2: nodesSwappedEvent.id2,
            });
          case StructureEventType.NODE_RENAMED:
            const nodeRenamedEvent = event as NodeRenamedEvent;
            return nodeRenamed({
              structureId: nodeRenamedEvent.structureId,
              version: nodeRenamedEvent.version,
              nodeId: nodeRenamedEvent.id,
              name: nodeRenamedEvent.name,
            });
          case StructureEventType.SNAPSHOTTED:
            const snapshotEvent = event as NodeRenamedEvent;
            return snapshotted({
              structureId: snapshotEvent.structureId,
              version: snapshotEvent.version,
            });
        }
      }),
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
