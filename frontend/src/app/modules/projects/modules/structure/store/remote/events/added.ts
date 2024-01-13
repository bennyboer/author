import { StructureEvent } from './event';
import { StructureEventType } from './structure-event-type';
import { StructureNodeId } from '../../state';

export class NodeAddedEvent implements StructureEvent {
  readonly type = StructureEventType.NODE_ADDED;

  constructor(
    public readonly structureId: string,
    public readonly parentNodeId: StructureNodeId,
    public readonly id: StructureNodeId,
    public readonly name: string,
  ) {}
}
