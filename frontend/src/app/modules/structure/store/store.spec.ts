import { TestBed, waitForAsync } from '@angular/core/testing';
import { StructureStoreEffects } from './effects';
import { StructureTreeService } from './service';
import { StoreModule } from '@ngrx/store';
import {
  LocalStorageRemoteStructureTreeService,
  structureStore,
  TreeService,
} from './index';
import { EffectsModule } from '@ngrx/effects';
import { filter, firstValueFrom } from 'rxjs';
import { LOCALSTORAGE_REMOTE_STRUCTURE_TREE_SERVICE_CONFIG } from './remote/localstorage.service';

describe('StructureStore', () => {
  const rootId = 'ROOT_ID';

  let effects: StructureStoreEffects;
  let service: StructureTreeService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot({}),
        EffectsModule.forRoot([]),

        StoreModule.forFeature(
          structureStore.featureName,
          structureStore.reducer,
        ),
        EffectsModule.forFeature([StructureStoreEffects]),
      ],
      providers: [
        StructureTreeService,
        {
          provide: TreeService,
          useClass: LocalStorageRemoteStructureTreeService,
        },
        {
          provide: LOCALSTORAGE_REMOTE_STRUCTURE_TREE_SERVICE_CONFIG,
          useValue: { delay: 0 },
        },
      ],
    });

    effects = TestBed.inject(StructureStoreEffects);
    service = TestBed.inject(StructureTreeService);
  }));

  it('should create effects and service', () => {
    expect(effects).toBeTruthy();
    expect(service).toBeTruthy();
  });

  it('should add node', async () => {
    // when: adding a node
    service.addNode(rootId, 'Child');

    // then: the node is added
    const updatedTree = await firstValueFrom(
      service.getTree().pipe(filter((tree) => tree.version === 1)),
    );

    expect(updatedTree.nodes[rootId].children.length).toBe(1);
    const childNodeId = updatedTree.nodes[rootId].children[0];
    expect(updatedTree.nodes[childNodeId].name).toBe('Child');
  }, 1000);

  it('should collapse node', async () => {
    // given: a tree with an expanded root node with a child
    service.addNode(rootId, 'Child');

    // when: toggling the root node
    service.toggleNode(rootId);

    // then: the root node is collapsed
    const updatedTree = await firstValueFrom(
      service.getTree().pipe(filter((tree) => tree.version === 2)),
    );

    expect(updatedTree.nodes[rootId].expanded).toBeFalsy();
  }, 1000);

  it('should remove node', async () => {
    // given: a tree with a root node with a child
    service.addNode(rootId, 'Child');
    const tree = await firstValueFrom(
      service.getTree().pipe(filter((tree) => tree.version === 1)),
    );
    const childNodeId = tree.nodes[rootId].children[0];

    // when: removing the child
    service.removeNode(childNodeId);

    // then: the child is removed
    const updatedTree = await firstValueFrom(
      service.getTree().pipe(filter((tree) => tree.version === 2)),
    );

    expect(updatedTree.nodes[rootId].children.length).toBe(0);
  }, 1000);

  it('should swap nodes', async () => {
    // given: a tree with a root node with two children
    service.addNode(rootId, 'Child 1');
    const treeVersion1 = await firstValueFrom(
      service.getTree().pipe(filter((tree) => tree.version === 1)),
    );
    const child1NodeId = treeVersion1.nodes[rootId].children[0];

    service.addNode(rootId, 'Child 2');
    const treeVersion2 = await firstValueFrom(
      service.getTree().pipe(filter((tree) => tree.version === 2)),
    );
    const child2NodeId = treeVersion2.nodes[rootId].children[1];

    // when: swapping the children
    service.swapNodes(child1NodeId, child2NodeId);

    // then: the children are swapped
    const updatedTree = await firstValueFrom(
      service.getTree().pipe(filter((tree) => tree.version === 3)),
    );

    expect(updatedTree.nodes[rootId].children[0]).toBe(child2NodeId);
    expect(updatedTree.nodes[rootId].children[1]).toBe(child1NodeId);
  }, 1000);
});
