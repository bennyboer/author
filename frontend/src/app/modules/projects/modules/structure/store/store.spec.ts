import { TestBed, waitForAsync } from '@angular/core/testing';
import { StructureStoreEffects } from './effects';
import { StructureService } from './service';
import { StoreModule } from '@ngrx/store';
import {
  LocalStorageStructureService,
  RemoteStructureService,
  structureStore,
} from './index';
import { EffectsModule } from '@ngrx/effects';
import { filter, firstValueFrom } from 'rxjs';
import { LOCALSTORAGE_REMOTE_STRUCTURE_SERVICE_CONFIG } from './remote/local-storage-structure.service';

describe('StructureStore', () => {
  const rootId = 'ROOT_ID';

  let effects: StructureStoreEffects;
  let service: StructureService;

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
        StructureService,
        {
          provide: RemoteStructureService,
          useClass: LocalStorageStructureService,
        },
        {
          provide: LOCALSTORAGE_REMOTE_STRUCTURE_SERVICE_CONFIG,
          useValue: { delay: 0, clearOnDestroy: true },
        },
      ],
    });

    effects = TestBed.inject(StructureStoreEffects);
    service = TestBed.inject(StructureService);
  }));

  it('should create effects and service', () => {
    expect(effects).toBeTruthy();
    expect(service).toBeTruthy();
  });

  it('should add node', async () => {
    // given: a loaded structure
    service.loadStructure('STRUCTURE_ID');
    await firstValueFrom(service.getStructure());

    // when: adding a node
    service.addNode(rootId, 'Child');

    // then: the node is added
    const updatedStructure = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 1)),
    );

    expect(updatedStructure.nodes[rootId].children.length).toBe(1);
    const childNodeId = updatedStructure.nodes[rootId].children[0];
    expect(updatedStructure.nodes[childNodeId].name).toBe('Child');
  }, 1000);

  it('should collapse node', async () => {
    // given: a structure with an expanded root node with a child
    service.loadStructure('STRUCTURE_ID');
    await firstValueFrom(service.getStructure());
    service.addNode(rootId, 'Child');
    await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 1)),
    );

    // when: toggling the root node
    service.toggleNode(rootId);

    // then: the root node is collapsed
    const updatedStructure = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 2)),
    );

    expect(updatedStructure.nodes[rootId].expanded).toBeFalsy();
  }, 1000);

  it('should remove node', async () => {
    // given: a structure with a root node with a child
    service.loadStructure('STRUCTURE_ID');
    await firstValueFrom(service.getStructure());
    service.addNode(rootId, 'Child');
    const structure = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 1)),
    );
    const childNodeId = structure.nodes[rootId].children[0];

    // when: removing the child
    service.removeNode(childNodeId);

    // then: the child is removed
    const updatedStructure = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 2)),
    );

    expect(updatedStructure.nodes[rootId].children.length).toBe(0);
  }, 1000);

  it('should rename node', async () => {
    // given: a structure with a root node
    service.loadStructure('STRUCTURE_ID');
    await firstValueFrom(service.getStructure());

    // when: renaming the root node
    service.renameNode(rootId, 'New Name');

    // then: the root node is renamed
    const updatedStructure = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 1)),
    );

    expect(updatedStructure.nodes[rootId].name).toBe('New Name');
  }, 1000);

  it('should swap nodes', async () => {
    // given: a structure with a root node with two children
    service.loadStructure('STRUCTURE_ID');
    await firstValueFrom(service.getStructure());
    service.addNode(rootId, 'Child 1');
    const structureVersion1 = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 1)),
    );
    const child1NodeId = structureVersion1.nodes[rootId].children[0];

    service.addNode(rootId, 'Child 2');
    const structureVersion2 = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 2)),
    );
    const child2NodeId = structureVersion2.nodes[rootId].children[1];

    // when: swapping the children
    service.swapNodes(child1NodeId, child2NodeId);

    // then: the children are swapped
    const updatedStructure = await firstValueFrom(
      service
        .getStructure()
        .pipe(filter((structure) => structure.version === 3)),
    );

    expect(updatedStructure.nodes[rootId].children[0]).toBe(child2NodeId);
    expect(updatedStructure.nodes[rootId].children[1]).toBe(child1NodeId);
  }, 1000);
});
