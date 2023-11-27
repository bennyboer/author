import { StructureTreeEvent } from './event';
import { EventType } from './event-type';
import { StructureTreeNodeId } from '../../state';

export class NodeAddedEvent implements StructureTreeEvent {
  readonly type = EventType.NODE_ADDED;
  constructor(
    public readonly parentNodeId: StructureTreeNodeId,
    public readonly id: StructureTreeNodeId,
    public readonly name: string,
  ) {}
}
