import { Anchor } from './anchor';

export class Bounds {
  readonly top: number;
  readonly left: number;
  readonly width: number;
  readonly height: number;

  constructor(props: {
    top: number;
    left: number;
    width: number;
    height: number;
  }) {
    this.top = props.top;
    this.left = props.left;
    this.width = props.width;
    this.height = props.height;
  }

  static zero(): Bounds {
    return new Bounds({ top: 0, left: 0, width: 0, height: 0 });
  }

  get bottom(): number {
    return this.top + this.height;
  }

  get right(): number {
    return this.left + this.width;
  }

  divide(quotient: number) {
    if (quotient === 0) {
      throw new Error('Cannot divide bounds by zero');
    }

    return new Bounds({
      top: this.top / quotient,
      left: this.left / quotient,
      width: this.width / quotient,
      height: this.height / quotient,
    });
  }

  multiply(factor: number): Bounds {
    return new Bounds({
      top: this.top * factor,
      left: this.left * factor,
      width: this.width * factor,
      height: this.height * factor,
    });
  }

  translate(x: number, y: number): Bounds {
    return new Bounds({
      top: this.top + y,
      left: this.left + x,
      width: this.width,
      height: this.height,
    });
  }

  contains(bounds: Bounds): boolean {
    return (
      this.top <= bounds.top &&
      this.left <= bounds.left &&
      this.bottom >= bounds.bottom &&
      this.right >= bounds.right
    );
  }

  containsAnchor(anchor: Anchor): boolean {
    return (
      this.top <= anchor.y &&
      this.left <= anchor.x &&
      this.bottom >= anchor.y &&
      this.right >= anchor.x
    );
  }

  intersects(bounds: Bounds): boolean {
    const cannotOverlapVertically =
      this.top > bounds.bottom || this.bottom < bounds.top;
    if (cannotOverlapVertically) {
      return false;
    }

    const cannotOverlapHorizontally =
      this.left > bounds.right || this.right < bounds.left;
    return !cannotOverlapHorizontally;
  }
}
