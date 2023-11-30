package de.bennyboer.author.structure.tree;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.structure.tree.commands.*;
import de.bennyboer.author.structure.tree.node.Node;
import de.bennyboer.author.structure.tree.node.NodeId;
import de.bennyboer.author.structure.tree.node.NodeName;
import de.bennyboer.eventsourcing.api.EventSourcingService;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentId;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentType;
import de.bennyboer.eventsourcing.api.persistence.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.util.List;

public class TreeService {

    EventSourcingService<Tree> eventSourcingService;

    public TreeService(EventSourcingRepo repo) {
        this.eventSourcingService = new EventSourcingService<>(
                Tree.TYPE,
                Tree.init(),
                repo,
                List.of()
        );
    }

    public Mono<Tree> get(TreeId id) {
        return eventSourcingService.aggregateLatest(AggregateId.of(id.getValue()));
    }

    public Mono<Tree> get(TreeId id, Version version) {
        return eventSourcingService.aggregate(AggregateId.of(id.getValue()), version);
    }

    public Mono<TreeId> create(NodeName rootNodeName, UserId userId) {
        TreeId id = TreeId.create();
        Node rootNode = Node.create(rootNodeName);

        return dispatchCommandToLatest(id, userId, CreateCmd.of(rootNode))
                .thenReturn(id);
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
        return dispatchCommand(id, version, userId, AddNodeCmd.of(parentNodeId, newNodeName));
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
