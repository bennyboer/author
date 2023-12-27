import { Observable } from 'rxjs';
import { StructureTreeEvent } from './events';
import { StructureTree } from '../state';

export abstract class RemoteTreeService {
  abstract getTree(treeId: string): Observable<StructureTree>;

  abstract getEvents(treeId: string): Observable<StructureTreeEvent>;

  abstract toggleNode(
    treeId: string,
    version: number,
    nodeId: string,
  ): Observable<void>;

  abstract addNode(
    treeId: string,
    version: number,
    parentNodeId: string,
    name: string,
  ): Observable<void>;

  abstract removeNode(
    treeId: string,
    version: number,
    nodeId: string,
  ): Observable<void>;

  abstract swapNodes(
    treeId: string,
    version: number,
    nodeId1: string,
    nodeId2: string,
  ): Observable<void>;

  abstract renameNode(
    treeId: string,
    version: number,
    nodeId: string,
    name: string,
  ): Observable<void>;

  abstract findTreeIdByProjectId(projectId: string): Observable<string>;
}
