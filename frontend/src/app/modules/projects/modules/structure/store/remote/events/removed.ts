import { StructureEvent } from './event';
import { StructureEventType } from './structure-event-type';
import { StructureNodeId } from '../../state';

export class NodeRemovedEvent implements StructureEvent {
  readonly type = StructureEventType.NODE_REMOVED;

  constructor(public readonly id: StructureNodeId) {}
}
