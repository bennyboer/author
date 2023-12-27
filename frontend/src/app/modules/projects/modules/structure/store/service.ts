import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { filter, Observable, tap } from 'rxjs';
import { StructureTree } from './state';
import { structureStore } from './index';
import { Option } from '../../../../shared';

@Injectable()
export class StructureTreeService {
  private treeId: Option<string> = Option.none();
  private version: Option<number> = Option.none();

  constructor(private readonly store: Store) {}

  loadTree(treeId: string): void {
    this.store.dispatch(structureStore.actions.loadTree({ treeId }));
  }

  getTree(): Observable<StructureTree> {
    return this.store.select(structureStore.selectors.tree).pipe(
      filter((tree) => !!tree),
      tap((tree) => {
        this.treeId = Option.some(tree.id);
        this.version = Option.some(tree.version);
      }),
    );
  }

  toggleNode(nodeId: string) {
    this.store.dispatch(
      structureStore.actions.toggleNode({
        treeId: this.treeId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId,
      }),
    );
  }

  addNode(parentNodeId: string, name: string) {
    this.store.dispatch(
      structureStore.actions.addNode({
        treeId: this.treeId.orElseThrow(),
        version: this.version.orElseThrow(),
        parentNodeId,
        name,
      }),
    );
  }

  removeNode(nodeId: string) {
    this.store.dispatch(
      structureStore.actions.removeNode({
        treeId: this.treeId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId,
      }),
    );
  }

  swapNodes(nodeId1: string, nodeId2: string) {
    this.store.dispatch(
      structureStore.actions.swapNodes({
        treeId: this.treeId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId1,
        nodeId2,
      }),
    );
  }

  renameNode(nodeId: string, name: string) {
    this.store.dispatch(
      structureStore.actions.renameNode({
        treeId: this.treeId.orElseThrow(),
        version: this.version.orElseThrow(),
        nodeId,
        name,
      }),
    );
  }
}
