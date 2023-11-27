import { StructureTreeEvent } from './event';
import { EventType } from './event-type';
import { StructureTreeNodeId } from '../../state';

export class NodeRemovedEvent implements StructureTreeEvent {
  readonly type = EventType.NODE_REMOVED;
  constructor(public readonly id: StructureTreeNodeId) {}
}
