import { RemoteStructureTreeService } from './remote.service';
import { EMPTY, map, Observable, tap } from 'rxjs';
import { StructureTreeEvent } from './events';
import {
  StructureTree,
  StructureTreeNode,
  StructureTreeNodeLookup,
} from '../state';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface AddChildRequest {
  name: string;
}

interface SwapNodesRequest {
  nodeId1: string;
  nodeId2: string;
}

type NodeId = string;

interface TreeDTO {
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
  implements RemoteStructureTreeService
{
  // TODO Fetch tree Id from project?
  private readonly treeId: string = 'bcfe6026-0966-431c-bbc9-8c970aa9c2d9'; // TODO Taken from server log
  private version: number = 0;

  constructor(private readonly http: HttpClient) {}

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
    return EMPTY; // TODO Take events from websocket
    // TODO Update version on events
  }

  getTree(): Observable<StructureTree> {
    return this.http.get<TreeDTO>(this.url(this.treeId)).pipe(
      map((tree) => this.mapToStructureTree(tree)),
      tap((tree) => (this.version = tree.version)),
    );
  }

  removeNode(nodeId: string): Observable<void> {
    return this.http.delete<void>(this.url(`${this.treeId}/nodes/${nodeId}`), {
      params: {
        version: this.version,
      },
    });
  }

  swapNodes(nodeId1: string, nodeId2: string): Observable<void> {
    return this.http.post<void>(
      this.url(`${this.treeId}/nodes/swap`),
      {
        nodeId1,
        nodeId2,
      },
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

  private url(postfix: string): string {
    const apiUrl = 'http://localhost:7070/api'; // TODO Take apiUrl from config
    return `${apiUrl}/structure/trees/${postfix}`;
  }

  private mapToStructureTree(tree: TreeDTO): StructureTree {
    return {
      version: 0,
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
        [id]: this.mapToStructureTreeNode(node),
      }),
      {},
    );
  }

  private mapToStructureTreeNode(node: NodeDTO): StructureTreeNode {
    return {
      id: node.name,
      name: node.name,
      expanded: node.expanded,
      children: node.children,
    };
  }
}
