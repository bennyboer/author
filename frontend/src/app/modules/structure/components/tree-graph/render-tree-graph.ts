import {
  ActionButtonType,
  RenderedActionButton,
  RenderNode,
  RenderNodeId,
} from './render-node';
import { RenderTreeGraphConfig } from './render-tree-graph-config';
import { Anchor, Bounds, Option, Size } from '../../../shared';

interface LayoutResult {
  width: number;
  height: number;
}

interface ActionButtonIdentifier {
  nodeId: RenderNodeId;
  type: ActionButtonType;
}

export class RenderTreeGraph {
  readonly nodes: Map<RenderNodeId, RenderNode>;
  readonly root: RenderNodeId;
  readonly config: RenderTreeGraphConfig;

  parentLookup: Map<RenderNodeId, RenderNodeId> = new Map<
    RenderNodeId,
    RenderNodeId
  >();
  size: Option<Size> = Option.none();
  visibleNodes: Set<RenderNodeId> = new Set<RenderNodeId>();
  hoveredNode: Option<RenderNodeId> = Option.none();
  focusedNode: Option<RenderNodeId> = Option.none();
  hoveredActionButton: Option<ActionButtonIdentifier> = Option.none();

  constructor(props: {
    nodes: Map<RenderNodeId, RenderNode>;
    root: RenderNodeId;
    config: RenderTreeGraphConfig;
  }) {
    this.nodes = props.nodes;
    this.root = props.root;
    this.config = props.config;

    if (!this.nodes.has(this.root)) {
      throw new Error(`Root node ${this.root} is not present in nodes map`);
    }

    this.layout();
  }

  render(
    ctx: CanvasRenderingContext2D,
    viewport: Bounds,
    swappableNodes: Option<Set<RenderNodeId>>,
  ) {
    this.visibleNodes.clear();
    this.renderNodeAndChildren(
      this.getRootNode(),
      ctx,
      viewport,
      swappableNodes,
    );
  }

  addNode(parentNodeId: RenderNodeId, node: RenderNode): RenderTreeGraph {
    const parentNode = this.nodes.get(parentNodeId)!;
    const updatedParentNode = parentNode.addChild(node.id);
    const updatedNodes = new Map(this.nodes);
    updatedNodes.set(parentNodeId, updatedParentNode);
    updatedNodes.set(node.id, node);

    return new RenderTreeGraph({
      nodes: updatedNodes,
      root: this.root,
      config: this.config,
    });
  }

  removeNode(id: RenderNodeId): RenderTreeGraph {
    if (id === this.root) {
      throw new Error(`Cannot remove root node ${id}`);
    }

    const updatedNodes = new Map(this.nodes);
    for (const node of this.nodes.values()) {
      if (node.children.includes(id)) {
        const updatedParentNode = node.removeChild(id);
        updatedNodes.set(node.id, updatedParentNode);
        break;
      }
    }

    this.removeNodeAndChildren(id, updatedNodes);

    return new RenderTreeGraph({
      nodes: updatedNodes,
      root: this.root,
      config: this.config,
    });
  }

  toggleNode(id: RenderNodeId): RenderTreeGraph {
    const node = this.nodes.get(id)!;
    const updatedNode = node.toggle();
    const updatedNodes = new Map(this.nodes);
    updatedNodes.set(id, updatedNode);

    return new RenderTreeGraph({
      nodes: updatedNodes,
      root: this.root,
      config: this.config,
    });
  }

  renameNode(id: RenderNodeId, name: string): RenderTreeGraph {
    const node = this.nodes.get(id)!;
    const updatedNode = node.rename(name);
    const updatedNodes = new Map(this.nodes);
    updatedNodes.set(id, updatedNode);

    return new RenderTreeGraph({
      nodes: updatedNodes,
      root: this.root,
      config: this.config,
    });
  }

  findNodeByAnchor(anchor: Anchor): Option<RenderNode> {
    for (const node of this.getVisibleNodes()) {
      if (node.bounds.containsAnchor(anchor)) {
        return Option.some(node);
      }
    }

    return Option.none();
  }

  findActionButtonByAnchor(anchor: Anchor): Option<RenderedActionButton> {
    for (const node of this.getVisibleNodes()) {
      const button = node.findActionButtonAt(anchor);
      if (button.isSome()) {
        return button;
      }
    }

    return Option.none();
  }

  tryUpdateHoveredNode(node: Option<RenderNodeId>): boolean {
    if (this.hoveredNode.equals(node)) {
      return false;
    }

    this.hoveredNode = node;
    return true;
  }

  tryUpdateFocusedNode(node: Option<RenderNodeId>): boolean {
    if (this.focusedNode.equals(node)) {
      return false;
    }

    this.focusedNode = node;
    return true;
  }

  tryUpdateHoveredActionButton(button: Option<RenderedActionButton>): boolean {
    const identifier: Option<ActionButtonIdentifier> = button.map((b) => ({
      nodeId: b.nodeId,
      type: b.type,
    }));

    if (this.hoveredActionButton.equals(identifier)) {
      return false;
    }

    this.hoveredActionButton.ifSome((identifier) => {
      this.nodes.get(identifier.nodeId)!.resetActionButtonsHoverState();
    });
    this.hoveredActionButton = identifier;
    return true;
  }

  isActionButtonHovered(): boolean {
    return this.hoveredActionButton.isSome();
  }

  findSwappableNodes(nodeId: string): Set<RenderNodeId> {
    const node = this.nodes.get(nodeId)!;

    const nodeAncestors = this.getAncestors(node);
    const nodeDescendants = this.getDescendants(node);
    const allNodeIds = new Set(this.nodes.keys());

    for (const id of nodeAncestors) {
      allNodeIds.delete(id);
    }
    for (const id of nodeDescendants) {
      allNodeIds.delete(id);
    }

    return allNodeIds;
  }

  swapNodes(node1: RenderNodeId, node2: RenderNodeId): RenderTreeGraph {
    const node1Parent = this.nodes.get(this.parentLookup.get(node1)!)!;
    const node2Parent = this.nodes.get(this.parentLookup.get(node2)!)!;

    const node1Index = node1Parent.children.indexOf(node1);
    const node2Index = node2Parent.children.indexOf(node2);

    const updatedNodes = new Map(this.nodes);

    if (node1Parent.id === node2Parent.id) {
      const updatedParentNode = node1Parent.swapChildren(
        node1Index,
        node2Index,
      );
      updatedNodes.set(node1Parent.id, updatedParentNode);
    } else {
      const updatedNode1Parent = node1Parent
        .removeChild(node1)
        .addChildAtIndex(node2, node1Index);
      const updatedNode2Parent = node2Parent
        .removeChild(node2)
        .addChildAtIndex(node1, node2Index);

      updatedNodes.set(node1Parent.id, updatedNode1Parent);
      updatedNodes.set(node2Parent.id, updatedNode2Parent);
    }

    return new RenderTreeGraph({
      nodes: updatedNodes,
      root: this.root,
      config: this.config,
    });
  }

  private getVisibleNodes(): RenderNode[] {
    return [...this.visibleNodes].map((id) => this.nodes.get(id)!);
  }

  private removeNodeAndChildren(
    id: RenderNodeId,
    nodes: Map<RenderNodeId, RenderNode>,
  ) {
    const node = nodes.get(id)!;

    for (const child of this.getChildren(node.id)) {
      this.removeNodeAndChildren(child.id, nodes);
    }

    nodes.delete(id);
  }

  private layout() {
    this.parentLookup.clear();
    const result = this.layoutNodeAndChildren(this.getRootNode(), 0, 0);
    this.size = Option.some(new Size(result));
  }

  private renderNodeAndChildren(
    node: RenderNode,
    ctx: CanvasRenderingContext2D,
    viewport: Bounds,
    swappableNodes: Option<Set<RenderNodeId>>,
  ) {
    const isInViewport = viewport.intersects(node.bounds);

    if (node.expanded) {
      for (const childNode of this.getChildren(node.id)) {
        const isChildNodeVisible = viewport.intersects(childNode.bounds);
        if (isInViewport || isChildNodeVisible) {
          this.renderConnection(node, childNode, ctx);
        }

        this.renderNodeAndChildren(childNode, ctx, viewport, swappableNodes);
      }
    }

    if (isInViewport || !this.config.renderOnlyVisible) {
      this.visibleNodes.add(node.id);

      const isHovered = this.hoveredNode
        .filter((hoveredNode) => hoveredNode === node.id)
        .isSome();
      const isFocused = this.focusedNode
        .filter((focusedNode) => focusedNode === node.id)
        .isSome();
      const isSwappable = swappableNodes.map((nodes) => nodes.has(node.id));

      ctx.save();
      node.render(
        ctx,
        isHovered,
        isFocused,
        this.hoveredActionButton
          .filter((b) => b.nodeId === node.id)
          .map((b) => b.type),
        node.id === this.root,
        isSwappable,
      );
      ctx.restore();
    }
  }

  private renderConnection(
    node: RenderNode,
    childNode: RenderNode,
    ctx: CanvasRenderingContext2D,
  ) {
    ctx.save();

    ctx.strokeStyle = '#CCC';
    ctx.lineWidth = 2;

    const start = node.getAnchor().translate(0, node.config.height);
    const end = childNode.getAnchor();

    ctx.beginPath();
    ctx.moveTo(start.x, start.y);
    ctx.lineTo(start.x, start.y + this.config.verticalSpacing / 2);
    ctx.lineTo(end.x, start.y + this.config.verticalSpacing / 2);
    ctx.lineTo(end.x, end.y);
    ctx.stroke();

    ctx.restore();
  }

  private getRootNode(): RenderNode {
    return this.nodes.get(this.root)!;
  }

  private getChildren(node: RenderNodeId): RenderNode[] {
    return this.nodes.get(node)!.children.map((id) => this.nodes.get(id)!);
  }

  private layoutNodeAndChildren(
    node: RenderNode,
    depth: number = 0,
    offset: number = 0,
  ): LayoutResult {
    const subTreeWidth = this.calculateSubTreeWidth(node);
    const halfSubTreeWidth = subTreeWidth / 2;
    const xOffset = offset + halfSubTreeWidth;
    const yOffset = depth * (this.config.verticalSpacing + node.config.height);
    node.layout(xOffset, yOffset);

    if (node.expanded) {
      let width = 0;
      const children = this.getChildren(node.id);
      for (let i = 0; i < children.length; i++) {
        const childNode = children[i];
        this.parentLookup.set(childNode.id, node.id);

        const result = this.layoutNodeAndChildren(
          childNode,
          depth + 1,
          offset + width,
        );

        width += result.width;
        if (i < children.length - 1) {
          width += this.config.horizontalSpacing;
        }
      }
    }

    return {
      width: node.expanded ? subTreeWidth : node.config.width,
      height: yOffset + node.config.height,
    };
  }

  private calculateSubTreeWidth(node: RenderNode): number {
    let leafCount = this.getExpandedLeafCount(node);

    return (
      leafCount * (node.config.width + this.config.horizontalSpacing) -
      this.config.horizontalSpacing
    );
  }

  private getExpandedLeafCount(node: RenderNode): number {
    if (!node.expanded) {
      return 1;
    }

    const children = this.getChildren(node.id);
    if (children.length === 0) {
      return 1;
    }

    return children.reduce(
      (acc, child) => acc + this.getExpandedLeafCount(child),
      0,
    );
  }

  private getAncestors(node: RenderNode): Set<RenderNodeId> {
    const result = new Set<RenderNodeId>();
    let currentNodeId = node.id;

    while (currentNodeId !== this.root) {
      const parentId = this.parentLookup.get(currentNodeId)!;
      result.add(parentId);
      currentNodeId = parentId;
    }

    return result;
  }

  private getDescendants(node: RenderNode): Set<RenderNodeId> {
    const result = new Set<RenderNodeId>();
    const stack = [node];

    while (stack.length > 0) {
      const currentNode = stack.pop()!;
      result.add(currentNode.id);

      for (const child of this.getChildren(currentNode.id)) {
        stack.push(child);
      }
    }

    return result;
  }
}
