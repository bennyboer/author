package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.server.structure.api.TreeDTO;
import de.bennyboer.author.server.structure.transformer.TreeTransformer;
import de.bennyboer.author.structure.tree.TreeId;
import de.bennyboer.author.structure.tree.TreeService;
import de.bennyboer.author.structure.tree.node.NodeId;
import de.bennyboer.author.structure.tree.node.NodeName;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class TreeFacade {

    TreeService treeService;

    public Mono<TreeDTO> getTree(String id) {
        TreeId treeId = TreeId.of(id);

        return treeService.get(treeId)
                .map(TreeTransformer::toApi);
    }

    public Mono<Void> create(String rootNodeName, Agent agent) {
        return treeService.create(NodeName.of(rootNodeName), agent).then();
    }

    public Mono<Void> toggleNode(String treeId, long version, String nodeId, Agent agent) {
        return treeService.toggleNode(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(nodeId),
                agent
        ).then();
    }

    public Mono<Void> addNode(String treeId, long version, String parentNodeId, String newNodeName, Agent agent) {
        return treeService.addNode(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(parentNodeId),
                NodeName.of(newNodeName),
                agent
        ).then();
    }

    public Mono<Void> removeNode(String treeId, long version, String nodeId, Agent agent) {
        return treeService.removeNode(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(nodeId),
                agent
        ).then();
    }

    public Mono<Void> swapNodes(String treeId, long version, String nodeId1, String nodeId2, Agent agent) {
        return treeService.swapNodes(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(nodeId1),
                NodeId.of(nodeId2),
                agent
        ).then();
    }

    public Mono<Void> renameNode(String treeId, Long treeVersion, String nodeId, String newNodeName, Agent agent) {
        return treeService.renameNode(
                TreeId.of(treeId),
                Version.of(treeVersion),
                NodeId.of(nodeId),
                NodeName.of(newNodeName),
                agent
        ).then();
    }

}
