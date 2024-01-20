import { Option } from '../../shared';

export class User {
  id: string;
  version: number;
  name: string;
  mail: string;
  password: string;
  firstName: string;
  lastName: string;
  imageId: Option<string>;

  constructor(props: {
    id: string;
    version: number;
    name: string;
    mail: string;
    password: string;
    firstName: string;
    lastName: string;
    imageId: Option<string>;
  }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.mail = props.mail;
    this.password = props.password;
    this.firstName = props.firstName;
    this.lastName = props.lastName;
    this.imageId = props.imageId;
  }
}
