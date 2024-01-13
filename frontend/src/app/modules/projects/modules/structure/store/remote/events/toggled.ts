import { StructureEvent } from './event';
import { StructureEventType } from './structure-event-type';
import { StructureNodeId } from '../../state';

export class NodeToggledEvent implements StructureEvent {
  readonly type = StructureEventType.NODE_TOGGLED;

  constructor(
    public readonly structureId: string,
    public readonly id: StructureNodeId,
  ) {}
}
