import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { filter, map, Observable, tap } from 'rxjs';
import { Structure } from './state';
import { structureStore } from './index';
import { Option } from '../../../../shared';

@Injectable()
export class StructureService {
  private structureId: Option<string> = Option.none();
  private version: Option<number> = Option.none();

  constructor(private readonly store: Store) {}

  loadStructure(structureId: string): void {
    this.store.dispatch(structureStore.actions.loadStructure({ structureId }));
  }

  getStructure(): Observable<Structure> {
    return this.store.select(structureStore.selectors.structure).pipe(
      filter((structure) => structure.isSome()),
      map((structure) => structure.orElseThrow()),
      tap((structure) => {
        this.structureId = Option.some(structure.id);
        this.version = Option.some(structure.version);
      }),
    );
  }

  toggleNode(nodeId: string) {
    this.store.dispatch(
      structureStore.actions.toggleNode({
        structureId: this.structureId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId,
      }),
    );
  }

  addNode(parentNodeId: string, name: string) {
    this.store.dispatch(
      structureStore.actions.addNode({
        structureId: this.structureId.orElseThrow(),
        version: this.version.orElseThrow(),
        parentNodeId,
        name,
      }),
    );
  }

  removeNode(nodeId: string) {
    this.store.dispatch(
      structureStore.actions.removeNode({
        structureId: this.structureId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId,
      }),
    );
  }

  swapNodes(nodeId1: string, nodeId2: string) {
    this.store.dispatch(
      structureStore.actions.swapNodes({
        structureId: this.structureId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId1,
        nodeId2,
      }),
    );
  }

  renameNode(nodeId: string, name: string) {
    this.store.dispatch(
      structureStore.actions.renameNode({
        structureId: this.structureId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId,
        name,
      }),
    );
  }
}
