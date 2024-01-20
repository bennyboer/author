import { UserEvent } from './event';
import { UserEventType } from './type';

export class RemovedEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.REMOVED;

  constructor(
    public readonly id: string,
    public readonly version: number,
  ) {}
}
