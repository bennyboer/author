import { UserEvent } from './event';
import { UserEventType } from './type';

export class MailUpdateRequestedEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.MAIL_UPDATE_REQUESTED;

  constructor(
    public readonly id: string,
    public readonly version: number,
    public readonly mail: string,
  ) {}
}
