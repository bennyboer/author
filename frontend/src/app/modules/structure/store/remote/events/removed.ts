import { StructureTreeEvent } from './event';
import { StructureTreeEventType } from './structure-tree-event-type';
import { StructureTreeNodeId } from '../../state';

export class NodeRemovedEvent implements StructureTreeEvent {
  readonly type = StructureTreeEventType.NODE_REMOVED;

  constructor(public readonly id: StructureTreeNodeId) {}
}
