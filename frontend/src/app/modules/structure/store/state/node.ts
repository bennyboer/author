export type StructureTreeNodeId = string;

export interface StructureTreeNode {
  id: StructureTreeNodeId;
  name: string;
  children: StructureTreeNodeId[];
  expanded: boolean;
}
