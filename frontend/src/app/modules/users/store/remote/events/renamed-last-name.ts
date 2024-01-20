import { UserEvent } from './event';
import { UserEventType } from './type';

export class RenamedLastNameEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.RENAMED_LAST_NAME;

  constructor(
    public readonly id: string,
    public readonly version: number,
    public readonly lastName: string,
  ) {}
}
