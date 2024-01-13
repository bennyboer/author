import { StructureEvent } from './event';
import { StructureEventType } from './structure-event-type';

export class SnapshottedEvent implements StructureEvent {
  readonly type = StructureEventType.SNAPSHOTTED;

  constructor(
    public readonly structureId: string,
    public readonly version: number,
  ) {}
}
