import { Option } from '../../shared';

export class User {
  id: string;
  name: string;
  mail: string;
  firstName: string;
  lastName: string;
  imageId: Option<string>;

  constructor(props: {
    id: string;
    name: string;
    mail: string;
    firstName: string;
    lastName: string;
    imageId: Option<string>;
  }) {
    this.id = props.id;
    this.name = props.name;
    this.mail = props.mail;
    this.firstName = props.firstName;
    this.lastName = props.lastName;
    this.imageId = props.imageId;
  }
}
