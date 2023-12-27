import { RemoteTreeService } from './tree.service';
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
import { StructureTree, StructureTreeNodeId, TreeMutator } from '../state';
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
  StructureTreeEvent,
} from './events';

export interface LocalStorageRemoteStructureTreeServiceConfig {
  delay: number;
}

export const LOCALSTORAGE_REMOTE_STRUCTURE_TREE_SERVICE_CONFIG =
  new InjectionToken<LocalStorageRemoteStructureTreeServiceConfig>(
    'LOCALSTORAGE_REMOTE_STRUCTURE_TREE_SERVICE_CONFIG',
  );

const LOCALSTORAGE_KEY = 'structure.tree';
const ROOT_ID: StructureTreeNodeId = 'ROOT_ID';
const DEFAULT_TREE: StructureTree = {
  id: 'LOCALSTORAGE_TREE_ID',
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
export class LocalStorageTreeService
  extends RemoteTreeService
  implements OnDestroy
{
  private readonly config: LocalStorageRemoteStructureTreeServiceConfig;

  private readonly tree$: BehaviorSubject<StructureTree> = new BehaviorSubject(
    DEFAULT_TREE,
  );
  private readonly events$: Subject<StructureTreeEvent> = new Subject();

  constructor(
    @Optional()
    @Inject(LOCALSTORAGE_REMOTE_STRUCTURE_TREE_SERVICE_CONFIG)
    config?: LocalStorageRemoteStructureTreeServiceConfig,
  ) {
    super();

    this.config = Option.someOrNone(config).orElse({ delay: 50 });

    this.loadTree();
  }

  ngOnDestroy() {
    this.events$.complete();
    this.tree$.complete();
  }

  getTree(treeId: string): Observable<StructureTree> {
    return this.tree$.asObservable().pipe(first());
  }

  getEvents(treeId: string): Observable<StructureTreeEvent> {
    return this.events$.asObservable();
  }

  addNode(
    treeId: string,
    version: number,
    parentNodeId: string,
    name: string,
  ): Observable<void> {
    const newNodeId = this.createUniqueId();

    return this.updateTreeAndFireEvent(
      new NodeAddedEvent(parentNodeId, newNodeId, name),
      (tree) => new TreeMutator(tree).addNode(parentNodeId, newNodeId, name),
    );
  }

  removeNode(
    treeId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.updateTreeAndFireEvent(new NodeRemovedEvent(nodeId), (tree) =>
      new TreeMutator(tree).removeNode(nodeId),
    );
  }

  swapNodes(
    treeId: string,
    version: number,
    nodeId1: string,
    nodeId2: string,
  ): Observable<void> {
    return this.updateTreeAndFireEvent(
      new NodesSwappedEvent(nodeId1, nodeId2),
      (tree) => new TreeMutator(tree).swapNodes(nodeId1, nodeId2),
    );
  }

  toggleNode(
    treeId: string,
    version: number,
    nodeId: string,
  ): Observable<void> {
    return this.updateTreeAndFireEvent(new NodeToggledEvent(nodeId), (tree) =>
      new TreeMutator(tree).toggleNode(nodeId),
    );
  }

  findTreeIdByProjectId(projectId: string): Observable<string> {
    return of('LOCALSTORAGE_TREE_ID');
  }

  override renameNode(
    treeId: string,
    version: number,
    nodeId: string,
    name: string,
  ): Observable<void> {
    return this.updateTreeAndFireEvent(
      new NodeRenamedEvent(nodeId, name),
      (tree) => new TreeMutator(tree).renameNode(nodeId, name),
    );
  }

  private createUniqueId(): string {
    if (!!crypto && !!crypto.randomUUID) {
      return crypto.randomUUID();
    } else {
      return Math.round(Math.random() * 10000000).toString(10);
    }
  }

  private updateTreeAndFireEvent(
    event: StructureTreeEvent,
    updateFn: (tree: StructureTree) => Option<StructureTree>,
  ): Observable<void> {
    return of(this.tree$.value).pipe(
      map((tree) => updateFn(tree)),
      delay(this.config.delay),
      tap((tree) => {
        tree.ifSome((t) => {
          this.saveTree(t);

          this.tree$.next(t);
          this.events$.next(event);
        });
      }),
      map(() => undefined),
    );
  }

  private loadTree() {
    Option.someOrNone(localStorage.getItem(LOCALSTORAGE_KEY))
      .map((treeStr) => JSON.parse(treeStr))
      .ifSome((tree) => {
        this.tree$.next(tree);
      });
  }

  private saveTree(tree: StructureTree) {
    localStorage.setItem(LOCALSTORAGE_KEY, JSON.stringify(tree));
  }
}
