import { UserEvent } from './event';
import { UserEventType } from './type';

export class ImageUpdatedEvent implements UserEvent {
  readonly type: UserEventType = UserEventType.IMAGE_UPDATED;

  constructor(
    public readonly id: string,
    public readonly version: number,
    public readonly imageId: string,
  ) {}
}
