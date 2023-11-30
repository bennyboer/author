package de.bennyboer.author.structure.tree;

import de.bennyboer.author.structure.tree.commands.*;
import de.bennyboer.author.structure.tree.events.*;
import de.bennyboer.author.structure.tree.node.Node;
import de.bennyboer.author.structure.tree.node.NodeId;
import de.bennyboer.author.structure.tree.node.NodeName;
import de.bennyboer.eventsourcing.api.aggregate.Aggregate;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.command.SnapshotCmd;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.Value;
import lombok.With;

import java.util.Map;

@Value
@With(AccessLevel.PRIVATE)
public class Tree implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("TREE");

    TreeId id;

    NodeId rootNodeId;

    Map<NodeId, Node> nodes;

    public static Tree init() {
        return new Tree(null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd) {
        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(this));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(c));
            case AddNodeCmd c -> ApplyCommandResult.of(NodeAddedEvent.of(c));
            case ToggleNodeCmd c -> ApplyCommandResult.of(NodeToggledEvent.of(c));
            case RemoveNodeCmd c -> ApplyCommandResult.of(NodeRemovedEvent.of(c));
            case SwapNodesCmd c -> ApplyCommandResult.of(NodesSwappedEvent.of(c));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        return switch (event) {
            case SnapshottedEvent e -> withId(TreeId.of(metadata.getAggregateId().getValue()))
                    .withRootNodeId(e.getRootNodeId())
                    .withNodes(e.getNodes());
            case CreatedEvent e -> withId(TreeId.of(metadata.getAggregateId().getValue()))
                    .withRootNodeId(e.getRootNode().getId())
                    .withNodes(Map.of(e.getRootNode().getId(), e.getRootNode()));
            case NodeAddedEvent e -> addNode(e.getParentNodeId(), e.getNewNodeName());
            case NodeToggledEvent e -> toggleNode(e.getNodeId());
            case NodeRemovedEvent e -> removeNode(e.getNodeId());
            case NodesSwappedEvent e -> swapNodes(e.getNodeId1(), e.getNodeId2());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };
    }

    public Node getRootNode() {
        return nodes.get(rootNodeId);
    }

    private Tree addNode(NodeId parentNodeId, NodeName newNodeName) {
        return this; // TODO
    }

    private Tree toggleNode(NodeId nodeId) {
        return this; // TODO
    }

    private Tree removeNode(NodeId nodeId) {
        return this; // TODO
    }

    private Tree swapNodes(NodeId nodeId1, NodeId nodeId2) {
        return this; // TODO
    }

}
