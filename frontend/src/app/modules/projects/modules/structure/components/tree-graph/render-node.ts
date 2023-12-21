import { Anchor, Bounds, Option, Size } from '../../../../../shared';
import { RenderNodeConfig } from './render-node-config';

export type RenderNodeId = string;

export enum ActionButtonType {
  ADD = 'ADD',
  REMOVE = 'REMOVE',
  TOGGLE = 'TOGGLE',
  SWAP = 'SWAP',
}

export interface RenderedActionButton {
  nodeId: RenderNodeId;
  type: ActionButtonType;
  center: Anchor;
  radius: number;
  hovered: boolean;
}

export class RenderNode {
  readonly id: RenderNodeId;
  readonly children: RenderNodeId[];
  readonly label: string;
  readonly config: RenderNodeConfig;

  expanded: boolean;
  private anchor: Option<Anchor> = Option.none();
  private size: Option<Size> = Option.none();

  private actionButtons: Map<ActionButtonType, RenderedActionButton> = new Map<
    ActionButtonType,
    RenderedActionButton
  >();

  constructor(props: {
    id: RenderNodeId;
    children: RenderNodeId[];
    label: string;
    expanded: boolean;
    config: RenderNodeConfig;
  }) {
    this.id = props.id;
    this.children = props.children;
    this.label = props.label;
    this.config = props.config;
    this.expanded = props.expanded;
  }

  get bounds(): Bounds {
    const anchor = this.getAnchor();
    const size = this.size.orElse(new Size({ width: 0, height: 0 }));

    const { width, height } = size;

    const x = anchor.x - width / 2;
    const y = anchor.y;

    return new Bounds({ top: y, left: x, width, height });
  }

  addChild(id: RenderNodeId): RenderNode {
    return new RenderNode({
      ...this,
      children: [...this.children, id],
    });
  }

  removeChild(id: RenderNodeId): RenderNode {
    return new RenderNode({
      ...this,
      children: this.children.filter((childId) => childId !== id),
    });
  }

  toggle(): RenderNode {
    return new RenderNode({
      ...this,
      expanded: !this.expanded,
    });
  }

  rename(name: string): RenderNode {
    return new RenderNode({
      ...this,
      label: name,
    });
  }

  findActionButtonAt(anchor: Anchor): Option<RenderedActionButton> {
    for (const button of this.actionButtons.values()) {
      const distance = button.center.distanceTo(anchor);
      if (distance <= button.radius) {
        return Option.some(button);
      }
    }

    return Option.none();
  }

  resetActionButtonsHoverState() {
    for (const button of this.actionButtons.values()) {
      button.hovered = false;
    }
  }

  getAnchor(): Anchor {
    return this.anchor.orElse(Anchor.zero());
  }

  swapChildren(index: number, withIndex: number): RenderNode {
    const newChildren = [...this.children];
    const tmp = newChildren[index];
    newChildren[index] = newChildren[withIndex];
    newChildren[withIndex] = tmp;

    return new RenderNode({
      ...this,
      children: newChildren,
    });
  }

  addChildAtIndex(node: RenderNodeId, index: number) {
    const newChildren = [...this.children];
    newChildren.splice(index, 0, node);

    return new RenderNode({
      ...this,
      children: newChildren,
    });
  }

  layout(x: number, y: number) {
    this.anchor = Option.some(new Anchor({ x, y }));

    this.size = Option.some(
      new Size({ width: this.config.width, height: this.config.height }),
    );
  }

  render(
    ctx: CanvasRenderingContext2D,
    hovered: boolean,
    focused: boolean,
    hoveredActionButton: Option<ActionButtonType>,
    isRoot: boolean,
    isSwappable: Option<boolean>,
  ) {
    const anchor = this.getAnchor();
    const size = this.size.orElse(new Size({ width: 0, height: 0 }));

    const { width, height } = size;

    const x = anchor.x - width / 2;
    const y = anchor.y;

    if (this.hasUnexpandedChildren()) {
      ctx.fillStyle = 'black';
      ctx.fillRect(x + 5, y + 5, width, height);

      ctx.fillStyle = 'white';
      ctx.fillRect(x + 1, y + 1, width, height);
    }

    ctx.fillStyle = 'black';
    if (isSwappable.isSome() && !isSwappable.orElseThrow()) {
      ctx.fillStyle = 'rgba(0, 0, 0, 0.1)';
    } else if (hovered) {
      ctx.fillStyle = '#333';
    }

    ctx.fillRect(x, y, width, height);

    const center = new Anchor({ x: x + width / 2, y: y + height / 2 });
    this.renderLabel(this.label, center, size, ctx);

    if (focused && isSwappable.isNone()) {
      ctx.strokeStyle = '#33FFCC';
      ctx.lineWidth = this.config.focusedBorderWidth;
      ctx.strokeRect(x, y, width, height);
    }

    this.renderActionButtons(
      ctx,
      anchor,
      size,
      hoveredActionButton,
      (hovered || focused) && isSwappable.isNone(),
      isRoot,
    );
  }

  private renderActionButton(
    ctx: CanvasRenderingContext2D,
    type: ActionButtonType,
    anchor: Anchor,
    isHovered: boolean,
    config: {
      color: string;
      hoverColor: string;
      radius: number;
      iconName: string;
    },
  ) {
    const { x, y } = anchor;
    const { color, hoverColor, radius, iconName } = config;

    ctx.beginPath();
    ctx.arc(x, y, radius, 0, 2 * Math.PI, false);
    ctx.fillStyle = isHovered ? hoverColor : color;
    ctx.fill();
    ctx.stroke();

    ctx.fillStyle = 'white';
    ctx.fillText(iconName, x, y);

    const addButton = this.actionButtons.get(type) ?? {
      nodeId: this.id,
      type,
      center: anchor,
      radius,
      hovered: isHovered,
    };
    addButton.center = anchor;
    addButton.radius = radius;
    this.actionButtons.set(type, addButton);
  }

  private renderActionButtons(
    ctx: CanvasRenderingContext2D,
    anchor: Anchor,
    size: Size,
    hoveredActionButton: Option<ActionButtonType>,
    showEditingButtons: boolean,
    isRoot: boolean,
  ) {
    if (!showEditingButtons) {
      this.actionButtons.delete(ActionButtonType.ADD);
      this.actionButtons.delete(ActionButtonType.REMOVE);
      this.actionButtons.delete(ActionButtonType.SWAP);
    }

    const { width, height } = size;
    const radius = 14;

    ctx.font = '24px Material Icons';
    ctx.strokeStyle = 'white';
    ctx.lineWidth = 2;

    if (showEditingButtons) {
      this.renderActionButton(
        ctx,
        ActionButtonType.ADD,
        anchor.translate(0, height),
        hoveredActionButton
          .map((type) => type === ActionButtonType.ADD)
          .orElse(false),
        {
          radius,
          color: 'black',
          hoverColor: '#333',
          iconName: 'add',
        },
      );

      if (!isRoot) {
        this.renderActionButton(
          ctx,
          ActionButtonType.REMOVE,
          anchor.translate(width / 2, 0),
          hoveredActionButton
            .map((type) => type === ActionButtonType.REMOVE)
            .orElse(false),
          {
            radius,
            color: '#FF3366',
            hoverColor: '#CC0033',
            iconName: 'remove',
          },
        );

        this.renderActionButton(
          ctx,
          ActionButtonType.SWAP,
          anchor.translate(width / 2 - (radius * 2 + 4), 0),
          hoveredActionButton
            .map((type) => type === ActionButtonType.SWAP)
            .orElse(false),
          {
            radius,
            color: 'black',
            hoverColor: '#333',
            iconName: 'swap_horiz',
          },
        );
      }
    }

    if (this.isToggleable()) {
      this.renderActionButton(
        ctx,
        ActionButtonType.TOGGLE,
        anchor.translate(-width / 2, 0),
        hoveredActionButton
          .map((type) => type === ActionButtonType.TOGGLE)
          .orElse(false),
        {
          radius,
          color: 'black',
          hoverColor: '#333',
          iconName: this.expanded ? 'expand_less' : 'expand_more',
        },
      );
    }
  }

  private renderLabel(
    label: string,
    center: Anchor,
    size: Size,
    ctx: CanvasRenderingContext2D,
  ) {
    const { fontSize, padding } = this.config;
    const { x, y } = center;
    const { width, height } = size;

    ctx.fillStyle = 'white';
    ctx.textBaseline = 'middle';
    ctx.textAlign = 'center';
    ctx.font = `${fontSize}px Lexend`;

    const textWidth = ctx.measureText(label).width;
    const totalWidth = textWidth + padding * 2;
    if (totalWidth <= width) {
      ctx.fillText(label, x, y);
    } else {
      const lineHeightFactor = 1.5;
      const lineHeight = fontSize * lineHeightFactor;

      const neededLines = this.calculateNeededLines(
        label,
        size,
        lineHeight,
        ctx,
      );
      const lines = this.splitIntoLines(label, neededLines);

      const lineWidths = lines.map((line) => ctx.measureText(line).width);
      const widestLineWidth = Math.max(...lineWidths);

      let scaledFontSize =
        widestLineWidth > width - padding * 2
          ? ((width - padding * 2) / widestLineWidth) * fontSize
          : fontSize;
      let scaledLineHeight = scaledFontSize * lineHeightFactor;

      const totalHeight = neededLines * scaledLineHeight;
      if (totalHeight > height - padding * 2) {
        scaledFontSize =
          ((height - padding * 2) / totalHeight) * scaledFontSize;
      }
      scaledLineHeight = scaledFontSize * lineHeightFactor;

      ctx.font = `${scaledFontSize}px Lexend`;

      for (let lineIdx = 0; lineIdx < lines.length; lineIdx++) {
        const line = lines[lineIdx];
        const lineY = y + (lineIdx - (lines.length - 1) / 2) * scaledLineHeight;
        ctx.fillText(line, x, lineY);
      }
    }
  }

  private splitIntoLines(label: string, lineCount: number): string[] {
    const splitAfterNCharacters = label.length / lineCount;

    const words = label.split(' ');
    const lines: string[] = [];
    let currentLine = '';
    for (let i = 0; i < words.length; i++) {
      const word = words[i];

      const lineLengthBefore = currentLine.length;
      const lineLengthAfter = currentLine.length + word.length + 1;
      const lineLengthBeforeDiff = Math.abs(
        lineLengthBefore - splitAfterNCharacters,
      );
      const lineLengthAfterDiff = Math.abs(
        lineLengthAfter - splitAfterNCharacters,
      );

      if (lineLengthAfterDiff <= lineLengthBeforeDiff) {
        if (currentLine.length === 0) {
          currentLine = word;
        } else {
          currentLine += ` ${word}`;
        }
      } else {
        lines.push(currentLine);
        currentLine = word;
      }
    }

    if (currentLine.length > 0) {
      lines.push(currentLine);
    }

    return lines;
  }

  private calculateNeededLines(
    label: string,
    size: Size,
    lineHeight: number,
    ctx: CanvasRenderingContext2D,
  ): number {
    const { width, height } = size;
    const textWidth = ctx.measureText(label).width;

    const neededRatio = width / height;

    let bestRatioDiff = Infinity;
    for (let lineCount = 1; ; lineCount++) {
      const w = textWidth / lineCount;
      const h = lineCount * lineHeight;
      const ratio = w / h;
      const ratioDiff = Math.abs(ratio - neededRatio);
      if (ratioDiff < bestRatioDiff) {
        bestRatioDiff = ratioDiff;
      } else {
        return lineCount - 1;
      }
    }
  }

  private hasUnexpandedChildren(): boolean {
    return this.isToggleable() && !this.expanded;
  }

  private isToggleable(): boolean {
    return this.children.length > 0;
  }
}
