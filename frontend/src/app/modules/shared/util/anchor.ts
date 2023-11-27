export class Anchor {
  readonly x: number;
  readonly y: number;

  constructor(props: { x: number; y: number }) {
    this.x = props.x;
    this.y = props.y;
  }

  static zero() {
    return new Anchor({ x: 0, y: 0 });
  }

  translate(x: number, y: number): Anchor {
    return new Anchor({ x: this.x + x, y: this.y + y });
  }

  distanceTo(anchor: Anchor): number {
    const dx = this.x - anchor.x;
    const dy = this.y - anchor.y;

    return Math.sqrt(dx * dx + dy * dy);
  }
}
