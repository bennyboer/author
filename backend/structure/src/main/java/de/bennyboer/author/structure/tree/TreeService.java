package de.bennyboer.author.structure.tree;

import de.bennyboer.author.structure.tree.commands.*;
import de.bennyboer.author.structure.tree.node.Node;
import de.bennyboer.author.structure.tree.node.NodeId;
import de.bennyboer.author.structure.tree.node.NodeName;
import de.bennyboer.eventsourcing.EventPublisher;
import de.bennyboer.eventsourcing.EventSourcingService;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.eventsourcing.aggregate.AggregateService;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.persistence.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.util.List;

public class TreeService extends AggregateService<Tree, TreeId> {

    public TreeService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Tree.TYPE,
                Tree.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<TreeId>> create(NodeName rootNodeName, Agent agent) {
        TreeId id = TreeId.create();
        Node rootNode = Node.createRoot(rootNodeName);

        return dispatchCommandToLatest(id, agent, CreateCmd.of(rootNode))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> toggleNode(TreeId id, Version version, NodeId nodeId, Agent agent) {
        return dispatchCommand(id, version, agent, ToggleNodeCmd.of(nodeId));
    }

    public Mono<Version> addNode(
            TreeId id,
            Version version,
            NodeId parentNodeId,
            NodeName newNodeName,
            Agent agent
    ) {
        NodeId newNodeId = NodeId.create();

        return dispatchCommand(id, version, agent, AddNodeCmd.of(parentNodeId, newNodeId, newNodeName));
    }

    public Mono<Version> removeNode(
            TreeId id,
            Version version,
            NodeId nodeId,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, RemoveNodeCmd.of(nodeId));
    }

    public Mono<Version> swapNodes(
            TreeId id,
            Version version,
            NodeId nodeId1,
            NodeId nodeId2,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, SwapNodesCmd.of(nodeId1, nodeId2));
    }

    public Mono<Version> renameNode(TreeId treeId, Version version, NodeId first, NodeName newName, Agent agent) {
        return dispatchCommand(treeId, version, agent, RenameNodeCmd.of(first, newName));
    }

    @Override
    protected AggregateId toAggregateId(TreeId treeId) {
        return AggregateId.of(treeId.getValue());
    }

    @Override
    protected boolean isRemoved(Tree aggregate) {
        return false;
    }

}
