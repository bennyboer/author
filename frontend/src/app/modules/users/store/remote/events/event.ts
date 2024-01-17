import { UserEventType } from './type';

export interface UserEvent {
  type: UserEventType;

  id: string;

  version: number;
}
