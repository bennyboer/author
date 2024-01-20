import { UserEvent } from './event';
import { UserEventType } from './type';

export class PasswordChangedEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.PASSWORD_CHANGED;

  constructor(
    public readonly id: string,
    public readonly version: number,
    public readonly password: string,
  ) {}
}
