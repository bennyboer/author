export class Size {
  readonly width: number;
  readonly height: number;

  constructor(props: { width: number; height: number }) {
    this.width = props.width;
    this.height = props.height;
  }
}
