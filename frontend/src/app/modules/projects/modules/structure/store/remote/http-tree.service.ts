import { RemoteTreeService } from './tree.service';
import { map, Observable, Subject } from 'rxjs';
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
import { WebSocketService } from '../../../../../shared';
import { EventMessage } from '../../../../../shared/services';
import { environment } from '../../../../../../../environments';

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
  id: string;
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
export class HttpTreeService implements RemoteTreeService, OnDestroy {
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly http: HttpClient,
    private readonly webSocketService: WebSocketService,
  ) {}

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  addNode(
    treeId: string,
    version: number,
    parentNodeId: string,
    name: string,
  ): Observable<void> {
    const request: AddChildRequest = { name };
    return this.http.post<void>(
      this.url(`${treeId}/nodes/${parentNodeId}/add-child`),
      request,
      {
        params: {
          version,
        },
      },
    );
  }

  getEvents(treeId: string): Observable<StructureTreeEvent> {
    return this.webSocketService
      .subscribeTo({ aggregateType: 'TREE', aggregateId: treeId })
      .pipe(map((msg) => this.mapToStructureTreeEvent(msg)));
  }

  getTree(treeId: string): Observable<StructureTree> {
    return this.http
      .get<TreeDTO>(this.url(treeId))
      .pipe(map((tree) => this.mapToStructureTree(tree)));
  }

  removeNode(
    treeId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.http.delete<void>(this.url(`${treeId}/nodes/${nodeId}`), {
      params: {
        version,
      },
    });
  }

  swapNodes(
    treeId: string,
    version: number,
    nodeId1: string,
    nodeId2: string,
  ): Observable<void> {
    const request: SwapNodesRequest = { nodeId1, nodeId2 };

    return this.http.post<void>(this.url(`${treeId}/nodes/swap`), request, {
      params: {
        version,
      },
    });
  }

  toggleNode(
    treeId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.http.post<void>(
      this.url(`${treeId}/nodes/${nodeId}/toggle`),
      {},
      {
        params: {
          version,
        },
      },
    );
  }

  renameNode(
    treeId: string,
    version: number,
    nodeId: string,
    name: string,
  ): Observable<void> {
    const request: RenameNodeRequest = { name };

    return this.http.post<void>(
      this.url(`${treeId}/nodes/${nodeId}/rename`),
      request,
      {
        params: {
          version,
        },
      },
    );
  }

  findTreeIdByProjectId(projectId: string): Observable<string> {
    return this.http.get(this.url(`by-project-id/${projectId}`), {
      responseType: 'text',
    });
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/structure/trees/${postfix}`;
  }

  private mapToStructureTree(tree: TreeDTO): StructureTree {
    return {
      id: tree.id,
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
