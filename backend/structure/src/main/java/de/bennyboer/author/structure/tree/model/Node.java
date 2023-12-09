package de.bennyboer.author.structure.tree.model;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Node {

    NodeId id;

    NodeName name;

    @Nullable
    NodeId parentId;

    List<NodeId> children;

    boolean expanded;

    public static Node of(
            NodeId id,
            NodeName name,
            @Nullable NodeId parentId,
            List<NodeId> children,
            boolean expanded
    ) {
        return new Node(id, name, parentId, children, expanded);
    }

    public static Node create(@Nullable NodeId parentId, NodeName name) {
        NodeId id = NodeId.create();

        return of(id, name, parentId, List.of(), true);
    }

    public static Node createRoot(NodeName name) {
        return create(null, name);
    }

    public Optional<NodeId> getParentId() {
        return Optional.ofNullable(parentId);
    }

    public Node addChild(NodeId nodeId) {
        var updatedChildren = new ArrayList<>(children);
        updatedChildren.add(nodeId);

        return withChildren(updatedChildren);
    }

    public Node removeChild(NodeId nodeId) {
        var updatedChildren = new ArrayList<>(children);
        updatedChildren.remove(nodeId);

        return withChildren(updatedChildren);
    }

    public Node toggle() {
        return withExpanded(!expanded);
    }

    public Node swapChildren(NodeId nodeId1, NodeId nodeId2) {
        var updatedChildren = new ArrayList<>(children);

        var index1 = updatedChildren.indexOf(nodeId1);
        var index2 = updatedChildren.indexOf(nodeId2);

        updatedChildren.set(index1, nodeId2);
        updatedChildren.set(index2, nodeId1);

        return withChildren(updatedChildren);
    }

    public Node rename(NodeName newNodeName) {
        return withName(newNodeName);
    }

}
