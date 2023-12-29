package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.permissions.StructurePermissionsService;
import de.bennyboer.author.structure.StructureId;
import de.bennyboer.author.structure.StructureService;
import de.bennyboer.author.structure.nodes.NodeId;
import de.bennyboer.author.structure.nodes.NodeName;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.structure.permissions.StructureAction.*;

@Value
@AllArgsConstructor
public class StructureCommandFacade {

    StructureService structureService;

    StructurePermissionsService permissionsService;

    public Mono<Void> toggleNode(String structureId, long version, String nodeId, Agent agent) {
        StructureId id = StructureId.of(structureId);

        return permissionsService.assertHasPermission(agent, TOGGLE_NODES, id)
                .then(structureService.toggleNode(
                        id,
                        Version.of(version),
                        NodeId.of(nodeId),
                        agent
                ))
                .then();
    }

    public Mono<Void> addNode(String structureId, long version, String parentNodeId, String newNodeName, Agent agent) {
        StructureId id = StructureId.of(structureId);

        return permissionsService.assertHasPermission(agent, ADD_NODES, id)
                .then(structureService.addNode(
                        id,
                        Version.of(version),
                        NodeId.of(parentNodeId),
                        NodeName.of(newNodeName),
                        agent
                ))
                .then();
    }

    public Mono<Void> removeNode(String structureId, long version, String nodeId, Agent agent) {
        StructureId id = StructureId.of(structureId);

        return permissionsService.assertHasPermission(agent, REMOVE_NODES, id)
                .then(structureService.removeNode(
                        id,
                        Version.of(version),
                        NodeId.of(nodeId),
                        agent
                ))
                .then();
    }

    public Mono<Void> swapNodes(String structureId, long version, String nodeId1, String nodeId2, Agent agent) {
        StructureId id = StructureId.of(structureId);

        return permissionsService.assertHasPermission(agent, SWAP_NODES, id)
                .then(structureService.swapNodes(
                        id,
                        Version.of(version),
                        NodeId.of(nodeId1),
                        NodeId.of(nodeId2),
                        agent
                ))
                .then();
    }

    public Mono<Void> renameNode(
            String structureId,
            long version,
            String nodeId,
            String newNodeName,
            Agent agent
    ) {
        StructureId id = StructureId.of(structureId);

        return permissionsService.assertHasPermission(agent, RENAME_NODES, id)
                .then(structureService.renameNode(
                        id,
                        Version.of(version),
                        NodeId.of(nodeId),
                        NodeName.of(newNodeName),
                        agent
                ))
                .then();
    }

}
