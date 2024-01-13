import { RemoteStructureService } from './structure.service';
import {
  BehaviorSubject,
  delay,
  first,
  map,
  Observable,
  of,
  Subject,
  tap,
} from 'rxjs';
import { Structure, StructureMutator, StructureNodeId } from '../state';
import { Option } from '../../../../../shared';
import {
  Inject,
  Injectable,
  InjectionToken,
  OnDestroy,
  Optional,
} from '@angular/core';
import {
  NodeAddedEvent,
  NodeRemovedEvent,
  NodeRenamedEvent,
  NodesSwappedEvent,
  NodeToggledEvent,
  StructureEvent,
} from './events';

export interface LocalStorageRemoteStructureServiceConfig {
  delay: number;
}

export const LOCALSTORAGE_REMOTE_STRUCTURE_SERVICE_CONFIG =
  new InjectionToken<LocalStorageRemoteStructureServiceConfig>(
    'LOCALSTORAGE_REMOTE_STRUCTURE_SERVICE_CONFIG',
  );

const LOCALSTORAGE_KEY = 'structure';
const ROOT_ID: StructureNodeId = 'ROOT_ID';
const DEFAULT_STRUCTURE: Structure = {
  id: 'LOCALSTORAGE_STRUCTURE_ID',
  version: 0,
  nodes: {
    [ROOT_ID]: {
      id: ROOT_ID,
      name: 'Project',
      children: [],
      expanded: true,
    },
  },
  rootId: ROOT_ID,
};

@Injectable()
export class LocalStorageStructureService
  extends RemoteStructureService
  implements OnDestroy
{
  private readonly config: LocalStorageRemoteStructureServiceConfig;

  private readonly structure$: BehaviorSubject<Structure> = new BehaviorSubject(
    DEFAULT_STRUCTURE,
  );
  private readonly events$: Subject<StructureEvent> = new Subject();

  constructor(
    @Optional()
    @Inject(LOCALSTORAGE_REMOTE_STRUCTURE_SERVICE_CONFIG)
    config?: LocalStorageRemoteStructureServiceConfig,
  ) {
    super();

    this.config = Option.someOrNone(config).orElse({ delay: 50 });

    this.loadStructure();
  }

  ngOnDestroy() {
    this.events$.complete();
    this.structure$.complete();
  }

  getStructure(structureId: string): Observable<Structure> {
    return this.structure$.asObservable().pipe(first());
  }

  getEvents(structureId: string): Observable<StructureEvent> {
    return this.events$.asObservable();
  }

  addNode(
    structureId: string,
    version: number,
    parentNodeId: string,
    name: string,
  ): Observable<void> {
    const newNodeId = this.createUniqueId();

    return this.updateStructureAndFireEvent(
      new NodeAddedEvent(
        structureId,
        version + 1,
        parentNodeId,
        newNodeId,
        name,
      ),
      (structure) =>
        new StructureMutator(structure).addNode(
          parentNodeId,
          newNodeId,
          name,
          version + 1,
        ),
    );
  }

  removeNode(
    structureId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.updateStructureAndFireEvent(
      new NodeRemovedEvent(structureId, version + 1, nodeId),
      (structure) =>
        new StructureMutator(structure).removeNode(nodeId, version),
    );
  }

  swapNodes(
    structureId: string,
    version: number,
    nodeId1: string,
    nodeId2: string,
  ): Observable<void> {
    return this.updateStructureAndFireEvent(
      new NodesSwappedEvent(structureId, version + 1, nodeId1, nodeId2),
      (structure) =>
        new StructureMutator(structure).swapNodes(
          nodeId1,
          nodeId2,
          version + 1,
        ),
    );
  }

  toggleNode(
    structureId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.updateStructureAndFireEvent(
      new NodeToggledEvent(structureId, version + 1, nodeId),
      (structure) =>
        new StructureMutator(structure).toggleNode(nodeId, version + 1),
    );
  }

  findStructureIdByProjectId(projectId: string): Observable<string> {
    return of('LOCALSTORAGE_STRUCTURE_ID');
  }

  override renameNode(
    structureId: string,
    version: number,
    nodeId: string,
    name: string,
  ): Observable<void> {
    return this.updateStructureAndFireEvent(
      new NodeRenamedEvent(structureId, version + 1, nodeId, name),
      (structure) =>
        new StructureMutator(structure).renameNode(nodeId, name, version + 1),
    );
  }

  private createUniqueId(): string {
    if (!!crypto && !!crypto.randomUUID) {
      return crypto.randomUUID();
    } else {
      return Math.round(Math.random() * 10000000).toString(10);
    }
  }

  private updateStructureAndFireEvent(
    event: StructureEvent,
    updateFn: (structure: Structure) => Option<Structure>,
  ): Observable<void> {
    return of(this.structure$.value).pipe(
      map((structure) => updateFn(structure)),
      delay(this.config.delay),
      tap((structure) => {
        structure.ifSome((t) => {
          this.saveStructure(t);

          this.structure$.next(t);
          this.events$.next(event);
        });
      }),
      map(() => undefined),
    );
  }

  private loadStructure() {
    Option.someOrNone(localStorage.getItem(LOCALSTORAGE_KEY))
      .map((structureStr) => JSON.parse(structureStr))
      .ifSome((structure) => {
        this.structure$.next(structure);
      });
  }

  private saveStructure(structure: Structure) {
    localStorage.setItem(LOCALSTORAGE_KEY, JSON.stringify(structure));
  }
}
