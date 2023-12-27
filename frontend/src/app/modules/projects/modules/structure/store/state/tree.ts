import { StructureTreeNode, StructureTreeNodeId } from './node';

export interface StructureTree {
  id: string;
  version: number;
  nodes: StructureTreeNodeLookup;
  rootId: StructureTreeNodeId;
}

export interface StructureTreeNodeLookup {
  [id: StructureTreeNodeId]: StructureTreeNode;
}
