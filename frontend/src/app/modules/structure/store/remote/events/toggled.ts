import { StructureTreeEvent } from './event';
import { EventType } from './event-type';
import { StructureTreeNodeId } from '../../state';

export class NodeToggledEvent implements StructureTreeEvent {
  readonly type = EventType.NODE_TOGGLED;
  constructor(public readonly id: StructureTreeNodeId) {}
}
