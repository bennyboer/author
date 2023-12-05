import { StructureTreeEvent } from './event';
import { StructureTreeEventType } from './structure-tree-event-type';
import { StructureTreeNodeId } from '../../state';

export class NodeAddedEvent implements StructureTreeEvent {
  readonly type = StructureTreeEventType.NODE_ADDED;

  constructor(
    public readonly parentNodeId: StructureTreeNodeId,
    public readonly id: StructureTreeNodeId,
    public readonly name: string,
  ) {}
}
