import { StructureEventType } from './structure-event-type';

export interface StructureEvent {
  structureId: string;
  type: StructureEventType;
}
