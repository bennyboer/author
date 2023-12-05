import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  Renderer2,
  ViewChild,
} from '@angular/core';
import {
  Anchor,
  Bounds,
  CanvasComponent,
  CanvasListenerResult,
  CanvasMouseEvent,
  Option,
} from '../../../shared';
import { RenderTreeGraph } from './render-tree-graph';
import {
  ActionButtonType,
  RenderedActionButton,
  RenderNode,
  RenderNodeId,
} from './render-node';
import { DEFAULT_RENDER_TREE_GRAPH_CONFIG } from './render-tree-graph-config';
import { DEFAULT_RENDER_NODE_CONFIG } from './render-node-config';
import {
  AddNodeCommand,
  RemoveNodeCommand,
  RenameNodeCommand,
  SwapNodesCommand,
  ToggleNodeCommand,
  TreeGraphCommand,
} from './commands';

export * from './commands';

export type TreeGraphNodeId = string;

export interface TreeGraph {
  nodes: Map<TreeGraphNodeId, TreeGraphNode>;
  root: TreeGraphNodeId;
}

export interface TreeGraphNode {
  id: TreeGraphNodeId;
  children: TreeGraphNodeId[];
  label: string;
  expanded: boolean;
}

const CLICK_MOVE_TOLERANCE: number = 5.0;
const MAX_DOUBLE_CLICK_TIME: number = 300.0;
const MARGIN_TOP: number = 50.0;

@Component({
  selector: 'app-tree-graph',
  templateUrl: './tree-graph.component.html',
  styleUrls: ['./tree-graph.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TreeGraphComponent {
  @Output()
  focusedNode: EventEmitter<Option<TreeGraphNode>> = new EventEmitter<
    Option<TreeGraphNode>
  >();

  @Output()
  doubleClickedNode: EventEmitter<TreeGraphNode> =
    new EventEmitter<TreeGraphNode>();

  @Output()
  command: EventEmitter<TreeGraphCommand> =
    new EventEmitter<TreeGraphCommand>();

  @Input()
  set tree(value: TreeGraph) {
    this.renderTree = Option.some(TreeGraphComponent.buildRenderTree(value));
    this.repaint();
  }

  @ViewChild(CanvasComponent)
  set canvasComponent(value: CanvasComponent) {
    this.canvas = Option.some(value);
  }

  private canvas: Option<CanvasComponent> = Option.none();
  private width: Option<number> = Option.none();

  private renderTree: Option<RenderTreeGraph> = Option.none();

  private mouseDownNode: Option<RenderNodeId> = Option.none();
  private mouseDownActionButton: Option<RenderedActionButton> = Option.none();
  private mouseDownStart: Option<Anchor> = Option.none();
  private lastClickTime: Option<number> = Option.none();
  private lastClickNode: Option<RenderNodeId> = Option.none();

  private nodeToSwap: Option<RenderNodeId> = Option.none();
  private swappableNodes: Option<Set<RenderNodeId>> = Option.none();

  constructor(
    private readonly elementRef: ElementRef,
    private readonly renderer: Renderer2,
  ) {}

  renameNode(nodeId: RenderNodeId, name: string) {
    this.command.emit(new RenameNodeCommand(nodeId, name));
  }

  render(ctx: CanvasRenderingContext2D, viewport: Bounds): void {
    ctx.save();

    this.renderTree.map((tree) => {
      const size = tree.size.orElse({ width: 0, height: 0 });
      const x = (ctx.canvas.width - size.width) / 2;
      ctx.translate(x, MARGIN_TOP);
      viewport = viewport.translate(-x, -MARGIN_TOP);

      tree.render(ctx, viewport, this.swappableNodes);
    });

    ctx.restore();

    this.width = Option.some(ctx.canvas.width);
  }

  mouseDown(event: CanvasMouseEvent): CanvasListenerResult {
    this.mouseDownStart = Option.some(
      new Anchor({ x: event.event.clientX, y: event.event.clientY }),
    );

    this.mouseDownActionButton = this.findActionButtonByAnchor(event.anchor);

    this.findNodeByAnchor(event.anchor)
      .filter(() => this.mouseDownActionButton.isNone())
      .ifSome((node) => {
        this.mouseDownNode = Option.some(node.id);
      });

    return { consumed: false };
  }

  mouseUp(event: CanvasMouseEvent): CanvasListenerResult {
    this.renderTree.ifSome((tree) => {
      const canBeClick = this.mouseDownStart
        .map((start) => {
          const dx = event.event.clientX - start.x;
          const dy = event.event.clientY - start.y;

          return Math.sqrt(dx * dx + dy * dy);
        })
        .map((distance) => distance < CLICK_MOVE_TOLERANCE)
        .orElse(false);

      if (canBeClick) {
        let isClicked = false;

        const actionButton = this.findActionButtonByAnchor(event.anchor);
        if (
          actionButton.isSome() &&
          this.mouseDownActionButton.equals(actionButton)
        ) {
          isClicked = true;
          this.onActionButtonClicked(actionButton.orElseThrow());
        }

        this.findNodeByAnchor(event.anchor)
          .filter(() => actionButton.isNone())
          .ifSome((node) => {
            isClicked = true;

            const changed = tree.tryUpdateFocusedNode(this.mouseDownNode);
            if (changed) {
              this.onNodeClicked(node);
              this.repaint();
            } else {
              this.lastClickNode
                .filter((n) => n === node.id)
                .ifSome(() => {
                  const now = performance.now();
                  this.lastClickTime
                    .filter((t) => now - t <= MAX_DOUBLE_CLICK_TIME)
                    .ifSome(() => {
                      this.onNodeDoubleClicked(node);

                      this.lastClickNode = Option.none();
                      this.lastClickTime = Option.none();
                    });
                });
            }

            this.lastClickNode = Option.some(node.id);
            this.lastClickTime = Option.some(performance.now());
          });

        if (!isClicked && event.isOnCanvas) {
          const changed = tree.tryUpdateFocusedNode(Option.none());
          if (changed) {
            this.focusedNode.emit(Option.none());
            this.repaint();
          }
        }
      }
    });

    this.mouseDownNode = Option.none();
    this.mouseDownStart = Option.none();

    return { consumed: false };
  }

  mouseMove(event: CanvasMouseEvent): CanvasListenerResult {
    this.renderTree.ifSome((tree) => {
      const actionButton = this.findActionButtonByAnchor(event.anchor);
      const changedHoveredActionButton =
        tree.tryUpdateHoveredActionButton(actionButton);
      if (changedHoveredActionButton) {
        this.repaint();
      }

      if (tree.isActionButtonHovered()) {
        this.renderer.setStyle(
          this.elementRef.nativeElement,
          'cursor',
          'pointer',
        );
      } else {
        this.renderer.setStyle(
          this.elementRef.nativeElement,
          'cursor',
          'default',
        );
      }

      const node = this.findNodeByAnchor(event.anchor)
        .map((n) => n.id)
        .or(actionButton.map((b) => b.nodeId));
      const changedHoveredNode = tree.tryUpdateHoveredNode(node);
      if (changedHoveredNode) {
        this.repaint();
      }
    });

    return { consumed: false };
  }

  private areCallbacksDisabled(): boolean {
    return this.swappableNodes.isSome();
  }

  private onActionButtonClicked(button: RenderedActionButton) {
    const nodeId = button.nodeId;

    switch (button.type) {
      case ActionButtonType.ADD:
        this.addNode(nodeId);
        break;
      case ActionButtonType.REMOVE:
        this.removeNode(nodeId);
        break;
      case ActionButtonType.TOGGLE:
        this.toggleNode(nodeId);
        break;
      case ActionButtonType.SWAP:
        this.initNodeSwapping(nodeId);
        break;
      default:
        throw new Error(`Unknown action button type: ${button.type}`);
    }
  }

  private onNodeClicked(node: RenderNode) {
    this.swappableNodes.ifSome((swappableNodes) => {
      if (swappableNodes.has(node.id)) {
        this.swapNodes(this.nodeToSwap.orElseThrow(), node.id);

        this.swappableNodes = Option.none();
      }
    });

    if (this.areCallbacksDisabled()) {
      return;
    }

    const treeGraphNode: TreeGraphNode = {
      id: node.id,
      children: node.children.map((id) => id as TreeGraphNodeId),
      label: node.label,
      expanded: node.expanded,
    };

    this.focusedNode.emit(Option.some(treeGraphNode));
  }

  private onNodeDoubleClicked(node: RenderNode) {
    if (this.areCallbacksDisabled()) {
      return;
    }

    const treeGraphNode: TreeGraphNode = {
      id: node.id,
      children: node.children.map((id) => id as TreeGraphNodeId),
      label: node.label,
      expanded: node.expanded,
    };

    this.doubleClickedNode.emit(treeGraphNode);
  }

  private findNodeByAnchor(anchor: Anchor): Option<RenderNode> {
    anchor = this.projectCanvasPositionToTreeGraphSpace(anchor);
    return this.renderTree.flatMap((tree) => tree.findNodeByAnchor(anchor));
  }

  private findActionButtonByAnchor(
    anchor: Anchor,
  ): Option<RenderedActionButton> {
    anchor = this.projectCanvasPositionToTreeGraphSpace(anchor);
    return this.renderTree.flatMap((tree) =>
      tree.findActionButtonByAnchor(anchor),
    );
  }

  private projectCanvasPositionToTreeGraphSpace(anchor: Anchor): Anchor {
    return this.renderTree
      .map((tree) => {
        const size = tree.size.orElse({ width: 0, height: 0 });
        const x = (this.width.orElse(0) - size.width) / 2;

        return anchor.translate(-x, -MARGIN_TOP);
      })
      .orElse(anchor);
  }

  private repaint() {
    this.canvas.map((c) => c.scheduleRepaint());
  }

  private static buildRenderTree(tree: TreeGraph): RenderTreeGraph {
    const nodes = new Map<RenderNodeId, RenderNode>();
    const root: RenderNodeId = tree.root;

    for (const node of tree.nodes.values()) {
      nodes.set(node.id, TreeGraphComponent.buildRenderNode(node));
    }

    return new RenderTreeGraph({
      nodes,
      root,
      config: DEFAULT_RENDER_TREE_GRAPH_CONFIG,
    });
  }

  private static buildRenderNode(node: TreeGraphNode): RenderNode {
    const id: RenderNodeId = node.id;
    const children: RenderNodeId[] = node.children;
    const label = node.label;
    const expanded = node.expanded;

    return new RenderNode({
      id,
      children,
      label,
      expanded,
      config: DEFAULT_RENDER_NODE_CONFIG,
    });
  }

  private addNode(nodeId: RenderNodeId) {
    this.command.emit(new AddNodeCommand(nodeId, '(New node)'));
  }

  private removeNode(nodeId: RenderNodeId) {
    this.command.emit(new RemoveNodeCommand(nodeId));
  }

  private toggleNode(nodeId: RenderNodeId) {
    this.command.emit(new ToggleNodeCommand(nodeId));
  }

  private initNodeSwapping(nodeId: string) {
    this.renderTree.ifSome((tree) => {
      const swappableNodes = tree.findSwappableNodes(nodeId);
      this.swappableNodes = Option.some(swappableNodes);
      this.nodeToSwap = Option.some(nodeId);

      this.repaint();
    });
  }

  private swapNodes(node1: RenderNodeId, node2: RenderNodeId) {
    this.command.emit(new SwapNodesCommand(node1, node2));
  }
}
