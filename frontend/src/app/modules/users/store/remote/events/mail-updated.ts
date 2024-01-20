import { UserEvent } from './event';
import { UserEventType } from './type';

export class MailUpdatedEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.MAIL_UPDATED;

  constructor(
    public readonly id: string,
    public readonly version: number,
    public readonly mail: string,
  ) {}
}
