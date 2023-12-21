export class Token {
  private readonly value: string;

  constructor(props: { value: string }) {
    this.value = props.value;
  }

  getValue(): string {
    return this.value;
  }
}
