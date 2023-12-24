package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.permissions.TreePermissionsService;
import de.bennyboer.author.structure.tree.TreeId;
import de.bennyboer.author.structure.tree.TreeService;
import de.bennyboer.author.structure.tree.nodes.NodeId;
import de.bennyboer.author.structure.tree.nodes.NodeName;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.structure.permissions.TreeAction.*;

@Value
@AllArgsConstructor
public class TreeCommandFacade {

    TreeService treeService;

    TreePermissionsService permissionsService;

    public Mono<Void> toggleNode(String treeId, long version, String nodeId, Agent agent) {
        TreeId id = TreeId.of(treeId);

        return permissionsService.assertHasPermission(agent, TOGGLE_NODES, id)
                .then(treeService.toggleNode(
                        TreeId.of(treeId),
                        Version.of(version),
                        NodeId.of(nodeId),
                        agent
                ))
                .then();
    }

    public Mono<Void> addNode(String treeId, long version, String parentNodeId, String newNodeName, Agent agent) {
        TreeId id = TreeId.of(treeId);

        return permissionsService.assertHasPermission(agent, ADD_NODES, id)
                .then(treeService.addNode(
                        TreeId.of(treeId),
                        Version.of(version),
                        NodeId.of(parentNodeId),
                        NodeName.of(newNodeName),
                        agent
                ))
                .then();
    }

    public Mono<Void> removeNode(String treeId, long version, String nodeId, Agent agent) {
        TreeId id = TreeId.of(treeId);

        return permissionsService.assertHasPermission(agent, REMOVE_NODES, id)
                .then(treeService.removeNode(
                        TreeId.of(treeId),
                        Version.of(version),
                        NodeId.of(nodeId),
                        agent
                ))
                .then();
    }

    public Mono<Void> swapNodes(String treeId, long version, String nodeId1, String nodeId2, Agent agent) {
        TreeId id = TreeId.of(treeId);

        return permissionsService.assertHasPermission(agent, SWAP_NODES, id)
                .then(treeService.swapNodes(
                        TreeId.of(treeId),
                        Version.of(version),
                        NodeId.of(nodeId1),
                        NodeId.of(nodeId2),
                        agent
                ))
                .then();
    }

    public Mono<Void> renameNode(String treeId, Long treeVersion, String nodeId, String newNodeName, Agent agent) {
        TreeId id = TreeId.of(treeId);

        return permissionsService.assertHasPermission(agent, RENAME_NODES, id)
                .then(treeService.renameNode(
                        TreeId.of(treeId),
                        Version.of(treeVersion),
                        NodeId.of(nodeId),
                        NodeName.of(newNodeName),
                        agent
                ))
                .then();
    }

}
