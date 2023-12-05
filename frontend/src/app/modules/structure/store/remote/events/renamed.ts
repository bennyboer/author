import { StructureTreeEvent } from './event';
import { StructureTreeEventType } from './structure-tree-event-type';
import { StructureTreeNodeId } from '../../state';

export class NodeRenamedEvent implements StructureTreeEvent {
  readonly type = StructureTreeEventType.NODE_RENAMED;

  constructor(
    public readonly id: StructureTreeNodeId,
    public readonly name: string,
  ) {}
}
