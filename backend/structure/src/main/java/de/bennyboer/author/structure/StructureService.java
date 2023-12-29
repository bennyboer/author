package de.bennyboer.author.structure;

import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.EventSourcingService;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.author.eventsourcing.aggregate.AggregateService;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.structure.create.CreateCmd;
import de.bennyboer.author.structure.nodes.Node;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
import de.bennyboer.author.structure.nodes.add.AddNodeCmd;
import de.bennyboer.author.structure.nodes.remove.RemoveNodeCmd;
import de.bennyboer.author.structure.nodes.rename.RenameNodeCmd;
import de.bennyboer.author.structure.nodes.swap.SwapNodesCmd;
import de.bennyboer.author.structure.nodes.toggle.ToggleNodeCmd;
import de.bennyboer.author.structure.remove.RemoveCmd;
import reactor.core.publisher.Mono;

import java.util.List;

public class StructureService extends AggregateService<Structure, StructureId> {

    public StructureService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Structure.TYPE,
                Structure.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<StructureId>> create(String projectId, NodeName rootNodeName, Agent agent) {
        StructureId id = StructureId.create();
        Node rootNode = Node.createRoot(rootNodeName);

        return dispatchCommandToLatest(id, agent, CreateCmd.of(projectId, rootNode))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> remove(StructureId structureId, Agent agent) {
        return dispatchCommandToLatest(structureId, agent, RemoveCmd.of());
    }

    public Mono<Version> toggleNode(StructureId id, Version version, NodeId nodeId, Agent agent) {
        return dispatchCommand(id, version, agent, ToggleNodeCmd.of(nodeId));
    }

    public Mono<Version> addNode(
            StructureId id,
            Version version,
            NodeId parentNodeId,
            NodeName newNodeName,
            Agent agent
    ) {
        NodeId newNodeId = NodeId.create();

        return dispatchCommand(id, version, agent, AddNodeCmd.of(parentNodeId, newNodeId, newNodeName));
    }

    public Mono<Version> removeNode(
            StructureId id,
            Version version,
            NodeId nodeId,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, RemoveNodeCmd.of(nodeId));
    }

    public Mono<Version> swapNodes(
            StructureId id,
            Version version,
            NodeId nodeId1,
            NodeId nodeId2,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, SwapNodesCmd.of(nodeId1, nodeId2));
    }

    public Mono<Version> renameNode(
            StructureId structureId,
            Version version,
            NodeId first,
            NodeName newName,
            Agent agent
    ) {
        return dispatchCommand(structureId, version, agent, RenameNodeCmd.of(first, newName));
    }

    @Override
    protected AggregateId toAggregateId(StructureId structureId) {
        return AggregateId.of(structureId.getValue());
    }

    @Override
    protected boolean isRemoved(Structure aggregate) {
        return aggregate.isRemoved();
    }

}
