export type StructureNodeId = string;

export interface StructureNode {
  id: StructureNodeId;
  name: string;
  children: StructureNodeId[];
  expanded: boolean;
}
