package de.bennyboer.author.structure.tree.node;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Node {

    NodeId id;

    NodeName name;

    List<NodeId> children;

    boolean expanded;

    public static Node of(NodeId id, NodeName name, List<NodeId> children, boolean expanded) {
        return new Node(id, name, children, expanded);
    }

    public static Node create(NodeName name) {
        NodeId id = NodeId.create();

        return of(id, name, List.of(), true);
    }

}
