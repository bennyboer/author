import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import {
  AddNodeCommand,
  RemoveNodeCommand,
  RenameNodeCommand,
  SwapNodesCommand,
  ToggleNodeCommand,
  TreeGraph,
  TreeGraphCommand,
  TreeGraphCommandType,
  TreeGraphComponent,
  TreeGraphNode,
  TreeGraphNodeId,
} from '../../components';
import { MatDialog } from '@angular/material/dialog';
import { NodeDetailsDialog } from '../../dialogs';
import { map, Observable, Subject, switchMap, takeUntil } from 'rxjs';
import { RemoteStructureService, StructureService } from '../../store';
import { Structure, StructureNode } from '../../store/state';
import { ActivatedRoute } from '@angular/router';

/*
TODO:
- A tree of the structure of the story starting with the title of the story.
- Afterwards the children of the title node are usually the Start, Main-part and Ending of the story.
- The scheme is repeated until we get nodes which we want to start writing at.
- Driven to excess, each node at the end is a single paragraph of the story.
- Each node is lockable on its own. That means when someone is currenly writing on a node, no other person is able to edit it. The other users should still be able to view the changes on-the-fly.
 */

@Component({
  templateUrl: './structure.page.html',
  styleUrls: ['./structure.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StructurePage implements OnInit, OnDestroy {
  @ViewChild(TreeGraphComponent)
  treeGraphComponent!: TreeGraphComponent;

  treeGraph$!: Observable<TreeGraph>;

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly structureService: StructureService,
    private readonly remoteStructureService: RemoteStructureService,
    private readonly route: ActivatedRoute,
    private readonly dialog: MatDialog,
  ) {}

  ngOnInit() {
    const projectId$ = this.route.params.pipe(
      map((params) => params['projectId']),
    );
    const structureId$ = projectId$.pipe(
      switchMap((projectId) =>
        this.remoteStructureService.findStructureIdByProjectId(projectId),
      ),
    );
    structureId$.subscribe((structureId) =>
      this.structureService.loadStructure(structureId),
    );

    this.treeGraph$ = this.structureService.getStructure().pipe(
      map((structure) => this.mapToTreeGraph(structure)),
      takeUntil(this.destroy$),
    );
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  openNodeDetails(node: TreeGraphNode) {
    const dialogRef = this.dialog.open(NodeDetailsDialog, {
      maxWidth: '600px',
    });
    dialogRef.componentInstance.label = node.label;
    dialogRef.componentInstance.labelChanges
      .pipe(takeUntil(dialogRef.afterClosed()))
      .subscribe((newLabel) =>
        this.treeGraphComponent.renameNode(node.id, newLabel),
      );
  }

  private mapToTreeGraph(structure: Structure): TreeGraph {
    const nodeList: [TreeGraphNodeId, TreeGraphNode][] = Object.values(
      structure.nodes,
    ).map((node) => {
      const mappedNode: TreeGraphNode = this.mapToTreeGraphNode(node);

      return [node.id, mappedNode];
    });

    const nodes = new Map<TreeGraphNodeId, TreeGraphNode>(nodeList);
    const root = structure.rootId;

    return {
      nodes,
      root,
    };
  }

  private mapToTreeGraphNode(node: StructureNode): TreeGraphNode {
    return {
      id: node.id,
      label: node.name,
      children: node.children,
      expanded: node.expanded,
    };
  }

  handleCommand(cmd: TreeGraphCommand) {
    switch (cmd.type) {
      case TreeGraphCommandType.ADD_NODE:
        const addNodeCmd = cmd as AddNodeCommand;
        this.structureService.addNode(addNodeCmd.parentNodeId, addNodeCmd.name);
        break;
      case TreeGraphCommandType.REMOVE_NODE:
        const removeNodeCmd = cmd as RemoveNodeCommand;
        this.structureService.removeNode(removeNodeCmd.nodeId);
        break;
      case TreeGraphCommandType.TOGGLE_NODE:
        const toggleNodeCmd = cmd as ToggleNodeCommand;
        this.structureService.toggleNode(toggleNodeCmd.nodeId);
        break;
      case TreeGraphCommandType.SWAP_NODES:
        const swapNodesCmd = cmd as SwapNodesCommand;
        this.structureService.swapNodes(
          swapNodesCmd.nodeId1,
          swapNodesCmd.nodeId2,
        );
        break;
      case TreeGraphCommandType.RENAME_NODE:
        const renameNodeCmd = cmd as RenameNodeCommand;
        this.structureService.renameNode(
          renameNodeCmd.nodeId,
          renameNodeCmd.name,
        );
        break;
    }
  }
}
