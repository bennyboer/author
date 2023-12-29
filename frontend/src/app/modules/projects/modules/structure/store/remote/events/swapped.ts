import { StructureEvent } from './event';
import { StructureEventType } from './structure-event-type';
import { StructureNodeId } from '../../state';

export class NodesSwappedEvent implements StructureEvent {
  readonly type = StructureEventType.NODES_SWAPPED;

  constructor(
    public readonly id1: StructureNodeId,
    public readonly id2: StructureNodeId,
  ) {}
}
