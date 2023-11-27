export type StructureTreeNodeId = string;

export const ROOT_ID: StructureTreeNodeId = 'ROOT_ID';

export interface StructureTreeNode {
  id: StructureTreeNodeId;
  name: string;
  children: StructureTreeNodeId[];
  expanded: boolean;
}
