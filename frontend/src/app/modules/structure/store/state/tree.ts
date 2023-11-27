import { StructureTreeNodeId, StructureTreeNode } from './node';

export interface StructureTree {
  version: number;
  nodes: StructureTreeNodeLookup;
  rootId: StructureTreeNodeId;
}

export interface StructureTreeNodeLookup {
  [id: StructureTreeNodeId]: StructureTreeNode;
}
