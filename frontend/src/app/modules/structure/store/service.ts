import { Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { filter, Observable } from 'rxjs';
import { StructureTree } from './state';
import { structureStore } from './index';

@Injectable()
export class StructureTreeService {
  constructor(private readonly store: Store) {}

  getTree(): Observable<StructureTree> {
    return this.store
      .select(structureStore.selectors.tree)
      .pipe(filter((tree) => !!tree));
  }

  toggleNode(nodeId: string) {
    this.store.dispatch(structureStore.actions.toggleNode({ nodeId }));
  }

  addNode(parentNodeId: string, name: string) {
    this.store.dispatch(structureStore.actions.addNode({ parentNodeId, name }));
  }

  removeNode(nodeId: string) {
    this.store.dispatch(structureStore.actions.removeNode({ nodeId }));
  }

  swapNodes(nodeId1: string, nodeId2: string) {
    this.store.dispatch(structureStore.actions.swapNodes({ nodeId1, nodeId2 }));
  }
}
