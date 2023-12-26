export type ProjectId = string;

export class Project {
  readonly id: ProjectId;
  readonly version: number;
  readonly name: string;

  constructor(props: { id: ProjectId; version: number; name: string }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
  }
}
