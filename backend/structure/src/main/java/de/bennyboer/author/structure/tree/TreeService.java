package de.bennyboer.author.structure.tree;

import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.EventSourcingService;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.author.eventsourcing.aggregate.AggregateService;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.structure.tree.create.CreateCmd;
import de.bennyboer.author.structure.tree.nodes.Node;
import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.structure.tree.nodes.NodeName;
import de.bennyboer.author.structure.tree.nodes.add.AddNodeCmd;
import de.bennyboer.author.structure.tree.nodes.remove.RemoveNodeCmd;
import de.bennyboer.author.structure.tree.nodes.rename.RenameNodeCmd;
import de.bennyboer.author.structure.tree.nodes.swap.SwapNodesCmd;
import de.bennyboer.author.structure.tree.nodes.toggle.ToggleNodeCmd;
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

    public Mono<AggregateIdAndVersion<TreeId>> create(String projectId, NodeName rootNodeName, Agent agent) {
        TreeId id = TreeId.create();
        Node rootNode = Node.createRoot(rootNodeName);

        return dispatchCommandToLatest(id, agent, CreateCmd.of(projectId, rootNode))
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
