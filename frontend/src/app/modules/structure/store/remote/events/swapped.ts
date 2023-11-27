import { StructureTreeEvent } from './event';
import { EventType } from './event-type';
import { StructureTreeNodeId } from '../../state';

export class NodesSwappedEvent implements StructureTreeEvent {
  readonly type = EventType.NODES_SWAPPED;
  constructor(
    public readonly id1: StructureTreeNodeId,
    public readonly id2: StructureTreeNodeId,
  ) {}
}
