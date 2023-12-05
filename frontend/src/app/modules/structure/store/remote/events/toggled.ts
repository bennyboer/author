import { StructureTreeEvent } from './event';
import { StructureTreeEventType } from './structure-tree-event-type';
import { StructureTreeNodeId } from '../../state';

export class NodeToggledEvent implements StructureTreeEvent {
  readonly type = StructureTreeEventType.NODE_TOGGLED;

  constructor(public readonly id: StructureTreeNodeId) {}
}
