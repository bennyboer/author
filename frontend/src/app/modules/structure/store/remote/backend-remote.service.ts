import { RemoteStructureTreeService } from './remote.service';
import { map, Observable, Subject, switchMap, takeUntil, tap } from 'rxjs';
import {
  NodeAddedEvent,
  NodeRemovedEvent,
  NodeRenamedEvent,
  NodesSwappedEvent,
  NodeToggledEvent,
  StructureTreeEvent,
  StructureTreeEventType,
} from './events';
import {
  StructureTree,
  StructureTreeNode,
  StructureTreeNodeLookup,
} from '../state';
import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { WebSocketService } from '../../../shared';
import { EventMessage } from '../../../shared/services';

interface AddChildRequest {
  name: string;
}

interface SwapNodesRequest {
  nodeId1: string;
  nodeId2: string;
}

interface RenameNodeRequest {
  name: string;
}

type NodeId = string;

interface TreeDTO {
  version: number;
  rootNodeId: string;
  nodes: NodeLookup;
}

interface NodeLookup {
  [id: NodeId]: NodeDTO;
}

interface NodeDTO {
  name: NodeId;
  children: NodeId[];
  expanded: boolean;
}

@Injectable()
export class BackendRemoteStructureTreeService
  implements RemoteStructureTreeService, OnDestroy
{
  // TODO Fetch tree Id from project?
  private readonly treeId: string = '86312cd5-39c2-4b2e-b315-547f07e0acc1'; // TODO Taken from server log
  private readonly events$: Subject<StructureTreeEvent> =
    new Subject<StructureTreeEvent>();
  private readonly tree$: Subject<StructureTree> = new Subject<StructureTree>();
  private readonly destroy$: Subject<void> = new Subject<void>();
  private version: number = 0;

  constructor(
    private readonly http: HttpClient,
    private readonly webSocketService: WebSocketService,
  ) {
    this.webSocketService
      .onConnected$()
      .pipe(
        switchMap(() => {
          return this.http.get<TreeDTO>(this.url(this.treeId)).pipe(
            map((tree) => this.mapToStructureTree(tree)),
            tap((tree) => {
              this.version = tree.version;
              this.tree$.next(tree);
            }),
            switchMap((tree) =>
              this.webSocketService.subscribeTo('TREE', this.treeId),
            ),
          );
        }),
        tap((msg) => {
          this.version = msg.topic.version;
        }),
        map((msg) => this.mapToStructureTreeEvent(msg)),
        takeUntil(this.destroy$),
      )
      .subscribe((event) => this.events$.next(event));
  }

  ngOnDestroy() {
    this.events$.complete();
    this.tree$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  addNode(parentNodeId: string, name: string): Observable<void> {
    const request: AddChildRequest = { name };
    return this.http.post<void>(
      this.url(`${this.treeId}/nodes/${parentNodeId}/add-child`),
      request,
      {
        params: {
          version: this.version,
        },
      },
    );
  }

  getEvents(): Observable<StructureTreeEvent> {
    return this.events$.asObservable();
  }

  getTree(): Observable<StructureTree> {
    return this.tree$.asObservable();
  }

  removeNode(nodeId: string): Observable<void> {
    return this.http.delete<void>(this.url(`${this.treeId}/nodes/${nodeId}`), {
      params: {
        version: this.version,
      },
    });
  }

  swapNodes(nodeId1: string, nodeId2: string): Observable<void> {
    const request: SwapNodesRequest = { nodeId1, nodeId2 };

    return this.http.post<void>(
      this.url(`${this.treeId}/nodes/swap`),
      request,
      {
        params: {
          version: this.version,
        },
      },
    );
  }

  toggleNode(nodeId: string): Observable<void> {
    return this.http.post<void>(
      this.url(`${this.treeId}/nodes/${nodeId}/toggle`),
      {},
      {
        params: {
          version: this.version,
        },
      },
    );
  }

  renameNode(nodeId: string, name: string): Observable<void> {
    const request: RenameNodeRequest = { name };

    return this.http.post<void>(
      this.url(`${this.treeId}/nodes/${nodeId}/rename`),
      request,
      {
        params: {
          version: this.version,
        },
      },
    );
  }

  private url(postfix: string): string {
    const apiUrl = 'http://localhost:7070/api'; // TODO Take apiUrl from config
    return `${apiUrl}/structure/trees/${postfix}`;
  }

  private mapToStructureTree(tree: TreeDTO): StructureTree {
    return {
      version: tree.version,
      rootId: tree.rootNodeId,
      nodes: this.mapToStructureTreeNodeLookup(tree.nodes),
    };
  }

  private mapToStructureTreeNodeLookup(
    nodes: NodeLookup,
  ): StructureTreeNodeLookup {
    return Object.entries(nodes).reduce(
      (lookup, [id, node]) => ({
        ...lookup,
        [id]: this.mapToStructureTreeNode(id, node),
      }),
      {},
    );
  }

  private mapToStructureTreeNode(id: NodeId, node: NodeDTO): StructureTreeNode {
    return {
      id,
      name: node.name,
      expanded: node.expanded,
      children: node.children,
    };
  }

  private mapToStructureTreeEvent(msg: EventMessage): StructureTreeEvent {
    const type = this.mapToStructureTreeEventType(msg.eventName);
    const payload = msg.payload;

    switch (type) {
      case StructureTreeEventType.NODE_ADDED:
        return new NodeAddedEvent(
          payload.parentNodeId,
          payload.newNodeId,
          payload.newNodeName,
        );
      case StructureTreeEventType.NODE_REMOVED:
        return new NodeRemovedEvent(payload.nodeId);
      case StructureTreeEventType.NODES_SWAPPED:
        return new NodesSwappedEvent(payload.nodeId1, payload.nodeId2);
      case StructureTreeEventType.NODE_TOGGLED:
        return new NodeToggledEvent(payload.nodeId);
      case StructureTreeEventType.NODE_RENAMED:
        return new NodeRenamedEvent(payload.nodeId, payload.newNodeName);
      default:
        throw new Error(`Unknown event type: ${type}`);
    }
  }

  private mapToStructureTreeEventType(
    eventName: string,
  ): StructureTreeEventType {
    switch (eventName) {
      case 'NODE_ADDED':
        return StructureTreeEventType.NODE_ADDED;
      case 'NODE_REMOVED':
        return StructureTreeEventType.NODE_REMOVED;
      case 'NODES_SWAPPED':
        return StructureTreeEventType.NODES_SWAPPED;
      case 'NODE_TOGGLED':
        return StructureTreeEventType.NODE_TOGGLED;
      case 'NODE_RENAMED':
        return StructureTreeEventType.NODE_RENAMED;
      default:
        throw new Error(`Unknown event name: ${eventName}`);
    }
  }
}
