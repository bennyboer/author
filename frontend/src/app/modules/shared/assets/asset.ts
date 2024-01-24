export class Asset {
  readonly id: string;
  readonly version: number;
  readonly content: string;
  readonly contentType: string;
  readonly createdAt: Date;

  constructor(
    id: string,
    version: number,
    content: string,
    contentType: string,
    createdAt: Date,
  ) {
    this.id = id;
    this.version = version;
    this.content = content;
    this.contentType = contentType;
    this.createdAt = createdAt;
  }

  toDataUri(): string {
    return `data:${this.contentType};base64,${this.content}`;
  }
}
