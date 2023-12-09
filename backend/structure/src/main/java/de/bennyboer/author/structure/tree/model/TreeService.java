package de.bennyboer.author.structure.tree.model;

import de.bennyboer.author.structure.tree.commands.*;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.api.EventPublisher;
import de.bennyboer.eventsourcing.api.EventSourcingService;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentId;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentType;
import de.bennyboer.eventsourcing.api.persistence.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.util.List;

public class TreeService {

    public static final AggregateType AGGREGATE_TYPE = Tree.TYPE;

    EventSourcingService<Tree> eventSourcingService;

    public TreeService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        this.eventSourcingService = new EventSourcingService<>(
                Tree.TYPE,
                Tree.init(),
                repo,
                eventPublisher,
                List.of()
        );
    }

    public Mono<Tree> get(TreeId id) {
        return eventSourcingService.aggregateLatest(AggregateId.of(id.getValue()));
    }

    public Mono<Tree> get(TreeId id, Version version) {
        return eventSourcingService.aggregate(AggregateId.of(id.getValue()), version);
    }

    public Mono<TreeIdAndVersion> create(NodeName rootNodeName, UserId userId) {
        TreeId id = TreeId.create();
        Node rootNode = Node.createRoot(rootNodeName);

        return dispatchCommandToLatest(id, userId, CreateCmd.of(rootNode))
                .map(version -> TreeIdAndVersion.of(id, version));
    }

    public Mono<Version> toggleNode(TreeId id, Version version, NodeId nodeId, UserId userId) {
        return dispatchCommand(id, version, userId, ToggleNodeCmd.of(nodeId));
    }

    public Mono<Version> addNode(
            TreeId id,
            Version version,
            NodeId parentNodeId,
            NodeName newNodeName,
            UserId userId
    ) {
        NodeId newNodeId = NodeId.create();

        return dispatchCommand(id, version, userId, AddNodeCmd.of(parentNodeId, newNodeId, newNodeName));
    }

    public Mono<Version> removeNode(
            TreeId id,
            Version version,
            NodeId nodeId,
            UserId userId
    ) {
        return dispatchCommand(id, version, userId, RemoveNodeCmd.of(nodeId));
    }

    public Mono<Version> swapNodes(
            TreeId id,
            Version version,
            NodeId nodeId1,
            NodeId nodeId2,
            UserId userId
    ) {
        return dispatchCommand(id, version, userId, SwapNodesCmd.of(nodeId1, nodeId2));
    }

    public Mono<Version> renameNode(TreeId treeId, Version version, NodeId first, NodeName newName, UserId userId) {
        return dispatchCommand(treeId, version, userId, RenameNodeCmd.of(first, newName));
    }

    private Mono<Version> dispatchCommand(TreeId id, Version version, UserId userId, Command cmd) {
        AggregateId aggregateId = AggregateId.of(id.getValue());
        var agent = Agent.of(AgentType.USER, AgentId.of(userId.getValue()));

        return eventSourcingService.dispatchCommand(aggregateId, version, cmd, agent);
    }

    private Mono<Version> dispatchCommandToLatest(TreeId id, UserId userId, Command cmd) {
        AggregateId aggregateId = AggregateId.of(id.getValue());
        var agent = Agent.of(AgentType.USER, AgentId.of(userId.getValue()));

        return eventSourcingService.dispatchCommandToLatest(aggregateId, cmd, agent);
    }

}
