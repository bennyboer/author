export class RenderNodeConfig {
  readonly width: number;
  readonly height: number;
  readonly fontSize: number;
  readonly padding: number;
  readonly focusedBorderWidth: number;

  constructor(props: {
    width: number;
    height: number;
    fontSize: number;
    padding: number;
    focusedBorderWidth: number;
  }) {
    this.width = props.width;
    this.height = props.height;
    this.fontSize = props.fontSize;
    this.padding = props.padding;
    this.focusedBorderWidth = props.focusedBorderWidth;
  }
}

export const DEFAULT_RENDER_NODE_CONFIG = new RenderNodeConfig({
  width: 200,
  height: 100,
  fontSize: 20,
  padding: 8,
  focusedBorderWidth: 4,
});
