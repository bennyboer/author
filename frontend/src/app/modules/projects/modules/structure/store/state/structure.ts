import { StructureNode, StructureNodeId } from './node';

export interface Structure {
  id: string;
  version: number;
  nodes: StructureNodeLookup;
  rootId: StructureNodeId;
}

export interface StructureNodeLookup {
  [id: StructureNodeId]: StructureNode;
}
