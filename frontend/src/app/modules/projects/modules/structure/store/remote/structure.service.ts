import { Observable } from 'rxjs';
import { StructureEvent } from './events';
import { Structure } from '../state';

export abstract class RemoteStructureService {
  abstract getStructure(structureId: string): Observable<Structure>;

  abstract getEvents(structureId: string): Observable<StructureEvent>;

  abstract toggleNode(
    structureId: string,
    version: number,
    nodeId: string,
  ): Observable<void>;

  abstract addNode(
    structureId: string,
    version: number,
    parentNodeId: string,
    name: string,
  ): Observable<void>;

  abstract removeNode(
    structureId: string,
    version: number,
    nodeId: string,
  ): Observable<void>;

  abstract swapNodes(
    structureId: string,
    version: number,
    nodeId1: string,
    nodeId2: string,
  ): Observable<void>;

  abstract renameNode(
    structureId: string,
    version: number,
    nodeId: string,
    name: string,
  ): Observable<void>;

  abstract findStructureIdByProjectId(projectId: string): Observable<string>;
}
