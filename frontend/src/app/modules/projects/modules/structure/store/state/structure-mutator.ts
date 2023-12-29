import { StructureNode, StructureNodeId } from './node';
import { Structure } from './structure';
import { Option } from '../../../../../shared';

export class StructureMutator {
  private readonly parentNodeIdLookup: Map<StructureNodeId, StructureNodeId> =
    new Map<StructureNodeId, StructureNodeId>();
  private readonly structure: Structure;

  constructor(structure: Structure) {
    this.structure = structure;
    this.updateParentNodeIdLookup();
  }

  addNode(
    parentNodeId: string,
    newNodeId: string,
    name: string,
  ): Option<Structure> {
    return this.findNodeById(parentNodeId).map((parentNode) => {
      const updatedNodes = {
        ...this.structure.nodes,
      };

      updatedNodes[parentNodeId] = {
        ...parentNode,
        children: [...parentNode.children, newNodeId],
      };

      updatedNodes[newNodeId] = {
        id: newNodeId,
        name,
        children: [],
        expanded: true,
      };

      return {
        ...this.structure,
        nodes: updatedNodes,
        version: this.structure.version + 1,
      };
    });
  }

  removeNode(nodeId: string): Option<Structure> {
    return this.findNodeParentId(nodeId).map((parentNodeId) => {
      const updatedNodes = {
        ...this.structure.nodes,
      };

      const parentNode = updatedNodes[parentNodeId];

      updatedNodes[parentNodeId] = {
        ...parentNode,
        children: parentNode.children.filter(
          (childNodeId) => childNodeId !== nodeId,
        ),
      };

      return {
        ...this.structure,
        nodes: updatedNodes,
        version: this.structure.version + 1,
      };
    });
  }

  toggleNode(nodeId: string): Option<Structure> {
    return this.findNodeById(nodeId).map((node) => {
      const updatedNodes = {
        ...this.structure.nodes,
      };

      updatedNodes[nodeId] = {
        ...node,
        expanded: !node.expanded,
      };

      return {
        ...this.structure,
        nodes: updatedNodes,
        version: this.structure.version + 1,
      };
    });
  }

  renameNode(nodeId: string, name: string): Option<Structure> {
    return this.findNodeById(nodeId).map((node) => {
      const updatedNodes = {
        ...this.structure.nodes,
      };

      updatedNodes[nodeId] = {
        ...node,
        name: name,
      };

      return {
        ...this.structure,
        nodes: updatedNodes,
        version: this.structure.version + 1,
      };
    });
  }

  swapNodes(nodeId1: string, nodeId2: string): Option<Structure> {
    if (
      this.isAncestor(nodeId1, nodeId2) ||
      this.isAncestor(nodeId2, nodeId1)
    ) {
      return Option.none();
    }

    const updatedNodes = {
      ...this.structure.nodes,
    };

    const parentNodeId1 = this.findNodeParentId(nodeId1).orElseThrow();
    const parentNodeId2 = this.findNodeParentId(nodeId2).orElseThrow();

    const parentNode1 = updatedNodes[parentNodeId1];
    const parentNode2 = updatedNodes[parentNodeId2];

    const node1Index = parentNode1.children.indexOf(nodeId1);
    const node2Index = parentNode2.children.indexOf(nodeId2);

    if (parentNodeId1 === parentNodeId2) {
      const updatedParentNode = {
        ...parentNode1,
        children: [...parentNode1.children],
      };

      updatedParentNode.children[node1Index] = nodeId2;
      updatedParentNode.children[node2Index] = nodeId1;

      updatedNodes[parentNodeId1] = updatedParentNode;
    } else {
      const updatedParentNode1 = {
        ...parentNode1,
        children: [...parentNode1.children],
      };
      const updatedParentNode2 = {
        ...parentNode2,
        children: [...parentNode2.children],
      };

      updatedParentNode1.children[node1Index] = nodeId2;
      updatedParentNode2.children[node2Index] = nodeId1;

      updatedNodes[parentNodeId1] = updatedParentNode1;
      updatedNodes[parentNodeId2] = updatedParentNode2;
    }

    return Option.some({
      ...this.structure,
      nodes: updatedNodes,
      version: this.structure.version + 1,
    });
  }

  private isAncestor(nodeId1: string, nodeId2: string): boolean {
    return this.findNodeParentId(nodeId2)
      .map((parentNodeId) => {
        if (parentNodeId === nodeId1) {
          return true;
        }

        return this.isAncestor(nodeId1, parentNodeId);
      })
      .orElse(false);
  }

  private findNodeParentId(nodeId: StructureNodeId): Option<StructureNodeId> {
    return Option.someOrNone(this.parentNodeIdLookup.get(nodeId));
  }

  private findNodeById(nodeId: StructureNodeId): Option<StructureNode> {
    return Option.someOrNone(this.structure.nodes[nodeId]);
  }

  private updateParentNodeIdLookup() {
    this.parentNodeIdLookup.clear();

    Object.values(this.structure.nodes).forEach((node) => {
      node.children.forEach((childNodeId) => {
        this.parentNodeIdLookup.set(childNodeId, node.id);
      });
    });
  }
}
