package de.bennyboer.author.structure.tree.api;

import de.bennyboer.author.structure.tree.commands.*;
import de.bennyboer.author.structure.tree.events.*;
import de.bennyboer.eventsourcing.api.aggregate.Aggregate;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.command.SnapshotCmd;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tree implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("TREE");

    TreeId id;

    long version;

    NodeId rootNodeId;

    Map<NodeId, Node> nodes;

    public static Tree init() {
        return new Tree(null, 0L, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd) {
        var isInitialized = Optional.ofNullable(id).isPresent();
        var isCreateCmd = cmd instanceof CreateCmd;
        if (!isInitialized && !isCreateCmd) {
            throw new IllegalStateException("Tree must be initialized with CreateCmd before applying other commands");
        }

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(this));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(c));
            case AddNodeCmd c -> {
                assertNodeExists(c.getParentNodeId());
                yield ApplyCommandResult.of(NodeAddedEvent.of(c));
            }
            case ToggleNodeCmd c -> {
                assertNodeExists(c.getNodeId());
                yield ApplyCommandResult.of(NodeToggledEvent.of(c));
            }
            case RemoveNodeCmd c -> {
                assertNodeExists(c.getNodeId());
                assertNodeIsNotRoot(c.getNodeId(), "Cannot remove root node");
                var parentNodeId = getNodeById(c.getNodeId()).orElseThrow().getParentId().orElseThrow();
                assertNodeExists(parentNodeId);
                yield ApplyCommandResult.of(NodeRemovedEvent.of(c));
            }
            case SwapNodesCmd c -> {
                assertNodeExists(c.getNodeId1());
                assertNodeExists(c.getNodeId2());
                assertNodesAreNotDirectlyRelated(c.getNodeId1(), c.getNodeId2());
                assertNodeIsNotRoot(c.getNodeId1(), "Cannot swap root node");
                assertNodeIsNotRoot(c.getNodeId2(), "Cannot swap root node");
                yield ApplyCommandResult.of(NodesSwappedEvent.of(c));
            }
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Tree apply(Event event, EventMetadata metadata) {
        var updatedTree = switch (event) {
            case SnapshottedEvent e -> withId(TreeId.of(metadata.getAggregateId().getValue()))
                    .withRootNodeId(e.getRootNodeId())
                    .withNodes(e.getNodes());
            case CreatedEvent e -> withId(TreeId.of(metadata.getAggregateId().getValue()))
                    .withRootNodeId(e.getRootNode().getId())
                    .withNodes(Map.of(e.getRootNode().getId(), e.getRootNode()));
            case NodeAddedEvent e -> addNode(e.getParentNodeId(), e.getNewNodeId(), e.getNewNodeName());
            case NodeToggledEvent e -> toggleNode(e.getNodeId());
            case NodeRemovedEvent e -> removeNode(e.getNodeId());
            case NodesSwappedEvent e -> swapNodes(e.getNodeId1(), e.getNodeId2());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };

        return updatedTree.withVersion(metadata.getAggregateVersion().getValue());
    }

    public Optional<Node> getNodeById(NodeId nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }

    public Node getRootNode() {
        return getNodeById(rootNodeId).orElseThrow();
    }

    private Tree addNode(NodeId parentNodeId, NodeId newNodeId, NodeName newNodeName) {
        var updatedNodes = new HashMap<>(nodes);

        var parentNode = getNodeById(parentNodeId).orElseThrow();
        var newNode = Node.of(newNodeId, newNodeName, parentNodeId, List.of(), true);
        var updatedParentNode = parentNode.addChild(newNode.getId());
        updatedNodes.put(parentNodeId, updatedParentNode);
        updatedNodes.put(newNode.getId(), newNode);

        return withNodes(updatedNodes);
    }

    private Tree toggleNode(NodeId nodeId) {
        var updatedNodes = new HashMap<>(nodes);

        var node = getNodeById(nodeId).orElseThrow();
        var updatedNode = node.toggle();
        updatedNodes.put(nodeId, updatedNode);

        return withNodes(updatedNodes);
    }

    private Tree removeNode(NodeId nodeId) {
        var updatedNodes = new HashMap<>(nodes);

        var node = getNodeById(nodeId).orElseThrow();
        var parentNodeId = node.getParentId().orElseThrow();
        var parentNode = getNodeById(parentNodeId).orElseThrow();
        var updatedParentNode = parentNode.removeChild(nodeId);
        updatedNodes.put(parentNode.getId(), updatedParentNode);
        updatedNodes.remove(nodeId);
        removeAllChildrenRecursively(nodeId, updatedNodes);

        return withNodes(updatedNodes);
    }

    private void removeAllChildrenRecursively(NodeId nodeId, Map<NodeId, Node> nodesToUpdate) {
        var node = getNodeById(nodeId).orElseThrow(() -> new IllegalArgumentException(
                "Cannot remove non-existing node"
        ));

        node.getChildren().forEach(childNodeId -> {
            removeAllChildrenRecursively(childNodeId, nodesToUpdate);
            nodesToUpdate.remove(childNodeId);
        });
    }

    private Tree swapNodes(NodeId nodeId1, NodeId nodeId2) {
        var updatedNodes = new HashMap<>(nodes);

        var node1 = getNodeById(nodeId1).orElseThrow();
        var node2 = getNodeById(nodeId2).orElseThrow();
        var parentNodeId1 = node1.getParentId().orElseThrow();
        var parentNodeId2 = node2.getParentId().orElseThrow();
        var parentNode1 = getNodeById(parentNodeId1).orElseThrow();
        var parentNode2 = getNodeById(parentNodeId2).orElseThrow();

        if (parentNode1.getId().equals(parentNode2.getId())) {
            var updatedParentNode = parentNode1.swapChildren(nodeId1, nodeId2);
            updatedNodes.put(parentNode1.getId(), updatedParentNode);
        } else {
            var updatedParentNode1 = parentNode1.removeChild(nodeId1).addChild(nodeId2);
            var updatedParentNode2 = parentNode2.removeChild(nodeId2).addChild(nodeId1);

            updatedNodes.put(parentNode1.getId(), updatedParentNode1);
            updatedNodes.put(parentNode2.getId(), updatedParentNode2);
        }

        return withNodes(updatedNodes);
    }

    private boolean isNodeDirectlyRelatedTo(NodeId nodeId1, NodeId nodeId2) {
        if (nodeId1.equals(nodeId2)) {
            return true;
        }
        if (nodeId1.equals(rootNodeId) || nodeId2.equals(rootNodeId)) {
            return true;
        }
        if (isChildOf(nodeId1, nodeId2) || isChildOf(nodeId2, nodeId1)) {
            return true;
        }

        return false;
    }

    private boolean isChildOf(NodeId potentialChildNodeId, NodeId potentialParentNodeId) {
        var potentialParentNode = getNodeById(potentialParentNodeId).orElseThrow(() -> new IllegalArgumentException(
                "Cannot check if node is child of non-existing node"
        ));

        for (var childNodeId : potentialParentNode.getChildren()) {
            if (childNodeId.equals(potentialChildNodeId)) {
                return true;
            }

            if (isChildOf(potentialChildNodeId, childNodeId)) {
                return true;
            }
        }

        return false;
    }

    private void assertNodeExists(NodeId nodeId) {
        if (!nodes.containsKey(nodeId)) {
            throw new IllegalArgumentException(String.format("Node with ID '%s' does not exist", nodeId.getValue()));
        }
    }

    private void assertNodeIsNotRoot(NodeId nodeId, String message) {
        if (nodeId.equals(rootNodeId)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void assertNodesAreNotDirectlyRelated(NodeId nodeId1, NodeId nodeId2) {
        if (isNodeDirectlyRelatedTo(nodeId1, nodeId2)) {
            throw new IllegalArgumentException("Nodes are directly related");
        }
    }

}