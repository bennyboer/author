import { UserEvent } from './event';
import { UserEventType } from './type';

export class RenamedFirstNameEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.RENAMED_FIRST_NAME;

  constructor(
    public readonly id: string,
    public readonly version: number,
    public readonly firstName: string,
  ) {}
}
