export class RenderTreeGraphConfig {
  readonly verticalSpacing: number;
  readonly horizontalSpacing: number;
  readonly renderOnlyVisible: boolean;

  constructor(props: {
    verticalSpacing: number;
    horizontalSpacing: number;
    renderOnlyVisible: boolean;
  }) {
    this.verticalSpacing = props.verticalSpacing;
    this.horizontalSpacing = props.horizontalSpacing;
    this.renderOnlyVisible = props.renderOnlyVisible;
  }
}

export const DEFAULT_RENDER_TREE_GRAPH_CONFIG = new RenderTreeGraphConfig({
  verticalSpacing: 60,
  horizontalSpacing: 40,
  renderOnlyVisible: true,
});
