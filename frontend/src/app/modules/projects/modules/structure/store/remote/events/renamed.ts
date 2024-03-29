import { StructureEvent } from './event';
import { StructureEventType } from './structure-event-type';
import { StructureNodeId } from '../../state';

export class NodeRenamedEvent implements StructureEvent {
  readonly type = StructureEventType.NODE_RENAMED;

  constructor(
    public readonly structureId: string,
    public readonly version: number,
    public readonly id: StructureNodeId,
    public readonly name: string,
  ) {}
}
