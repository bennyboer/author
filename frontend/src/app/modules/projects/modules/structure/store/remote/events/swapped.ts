import { StructureTreeEvent } from './event';
import { StructureTreeEventType } from './structure-tree-event-type';
import { StructureTreeNodeId } from '../../state';

export class NodesSwappedEvent implements StructureTreeEvent {
  readonly type = StructureTreeEventType.NODES_SWAPPED;

  constructor(
    public readonly id1: StructureTreeNodeId,
    public readonly id2: StructureTreeNodeId,
  ) {}
}
