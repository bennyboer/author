export type ProjectId = string;

export class Project {
  readonly id: ProjectId;
  readonly name: string;

  constructor(props: { id: ProjectId; name: string }) {
    this.id = props.id;
    this.name = props.name;
  }
}
