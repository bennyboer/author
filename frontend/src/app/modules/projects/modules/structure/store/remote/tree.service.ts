import { Observable } from 'rxjs';
import { StructureTreeEvent } from './events';
import { StructureTree } from '../state';

export abstract class TreeService {
  abstract getTree(): Observable<StructureTree>;

  abstract getEvents(): Observable<StructureTreeEvent>;

  abstract toggleNode(nodeId: string): Observable<void>;

  abstract addNode(parentNodeId: string, name: string): Observable<void>;

  abstract removeNode(nodeId: string): Observable<void>;

  abstract swapNodes(nodeId1: string, nodeId2: string): Observable<void>;

  abstract renameNode(nodeId: string, name: string): Observable<void>;
}
