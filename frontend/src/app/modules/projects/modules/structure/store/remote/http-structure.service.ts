import { RemoteStructureService } from './structure.service';
import { map, Observable, Subject } from 'rxjs';
import {
  NodeAddedEvent,
  NodeRemovedEvent,
  NodeRenamedEvent,
  NodesSwappedEvent,
  NodeToggledEvent,
  StructureEvent,
  StructureEventType,
} from './events';
import { Structure, StructureNode, StructureNodeLookup } from '../state';
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

interface StructureDTO {
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
export class HttpStructureService implements RemoteStructureService, OnDestroy {
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
    structureId: string,
    version: number,
    parentNodeId: string,
    name: string,
  ): Observable<void> {
    const request: AddChildRequest = { name };
    return this.http.post<void>(
      this.url(`${structureId}/nodes/${parentNodeId}/add-child`),
      request,
      {
        params: {
          version,
        },
      },
    );
  }

  getEvents(structureId: string): Observable<StructureEvent> {
    return this.webSocketService
      .subscribeTo({ aggregateType: 'STRUCTURE', aggregateId: structureId })
      .pipe(map((msg) => this.mapToStructureEvent(msg)));
  }

  getStructure(structureId: string): Observable<Structure> {
    return this.http
      .get<StructureDTO>(this.url(structureId))
      .pipe(map((structure) => this.mapToStructure(structure)));
  }

  removeNode(
    structureId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.http.delete<void>(this.url(`${structureId}/nodes/${nodeId}`), {
      params: {
        version,
      },
    });
  }

  swapNodes(
    structureId: string,
    version: number,
    nodeId1: string,
    nodeId2: string,
  ): Observable<void> {
    const request: SwapNodesRequest = { nodeId1, nodeId2 };

    return this.http.post<void>(
      this.url(`${structureId}/nodes/swap`),
      request,
      {
        params: {
          version,
        },
      },
    );
  }

  toggleNode(
    structureId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.http.post<void>(
      this.url(`${structureId}/nodes/${nodeId}/toggle`),
      {},
      {
        params: {
          version,
        },
      },
    );
  }

  renameNode(
    structureId: string,
    version: number,
    nodeId: string,
    name: string,
  ): Observable<void> {
    const request: RenameNodeRequest = { name };

    return this.http.post<void>(
      this.url(`${structureId}/nodes/${nodeId}/rename`),
      request,
      {
        params: {
          version,
        },
      },
    );
  }

  findStructureIdByProjectId(projectId: string): Observable<string> {
    return this.http.get(this.url(`by-project-id/${projectId}`), {
      responseType: 'text',
    });
  }

  private url(postfix: string): string {
    return `${environment.apiUrl}/structures/${postfix}`;
  }

  private mapToStructure(structure: StructureDTO): Structure {
    return {
      id: structure.id,
      version: structure.version,
      rootId: structure.rootNodeId,
      nodes: this.mapToStructureNodeLookup(structure.nodes),
    };
  }

  private mapToStructureNodeLookup(nodes: NodeLookup): StructureNodeLookup {
    return Object.entries(nodes).reduce(
      (lookup, [id, node]) => ({
        ...lookup,
        [id]: this.mapToStructureNode(id, node),
      }),
      {},
    );
  }

  private mapToStructureNode(id: NodeId, node: NodeDTO): StructureNode {
    return {
      id,
      name: node.name,
      expanded: node.expanded,
      children: node.children,
    };
  }

  private mapToStructureEvent(msg: EventMessage): StructureEvent {
    const structureId = msg.topic.aggregateId;
    const type = this.mapToStructureEventType(msg.eventName);
    const payload = msg.payload;

    switch (type) {
      case StructureEventType.NODE_ADDED:
        return new NodeAddedEvent(
          structureId,
          payload.parentNodeId,
          payload.newNodeId,
          payload.newNodeName,
        );
      case StructureEventType.NODE_REMOVED:
        return new NodeRemovedEvent(structureId, payload.nodeId);
      case StructureEventType.NODES_SWAPPED:
        return new NodesSwappedEvent(
          structureId,
          payload.nodeId1,
          payload.nodeId2,
        );
      case StructureEventType.NODE_TOGGLED:
        return new NodeToggledEvent(structureId, payload.nodeId);
      case StructureEventType.NODE_RENAMED:
        return new NodeRenamedEvent(
          structureId,
          payload.nodeId,
          payload.newNodeName,
        );
      default:
        throw new Error(`Unknown event type: ${type}`);
    }
  }

  private mapToStructureEventType(eventName: string): StructureEventType {
    switch (eventName) {
      case 'NODE_ADDED':
        return StructureEventType.NODE_ADDED;
      case 'NODE_REMOVED':
        return StructureEventType.NODE_REMOVED;
      case 'NODES_SWAPPED':
        return StructureEventType.NODES_SWAPPED;
      case 'NODE_TOGGLED':
        return StructureEventType.NODE_TOGGLED;
      case 'NODE_RENAMED':
        return StructureEventType.NODE_RENAMED;
      default:
        throw new Error(`Unknown event name: ${eventName}`);
    }
  }
}
