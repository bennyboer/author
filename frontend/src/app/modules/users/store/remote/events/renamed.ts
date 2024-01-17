import { UserEvent } from './event';
import { UserEventType } from './type';

export class UserNameChangedEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.USERNAME_CHANGED;

  constructor(
    public readonly id: string,
    public readonly version: number,
    public readonly name: string,
  ) {}
}
