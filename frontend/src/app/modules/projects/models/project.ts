export type ProjectId = string;

export class Project {
  readonly id: ProjectId;
  readonly version: number;
  readonly name: string;
  readonly createdAt: Date;

  constructor(props: {
    id: ProjectId;
    version: number;
    name: string;
    createdAt: Date;
  }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.createdAt = props.createdAt;
  }
}
