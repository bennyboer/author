package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.structure.api.TreeDTO;
import de.bennyboer.author.server.structure.transformer.TreeTransformer;
import de.bennyboer.author.structure.tree.api.NodeId;
import de.bennyboer.author.structure.tree.api.NodeName;
import de.bennyboer.author.structure.tree.api.TreeId;
import de.bennyboer.author.structure.tree.api.TreeService;
import de.bennyboer.eventsourcing.api.Version;
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

    public Mono<Void> create(String rootNodeName, UserId userId) {
        return treeService.create(NodeName.of(rootNodeName), userId).then();
    }

    public Mono<Void> toggleNode(String treeId, long version, String nodeId, UserId userId) {
        return treeService.toggleNode(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(nodeId),
                userId
        ).then();
    }

    public Mono<Void> addNode(String treeId, long version, String parentNodeId, String newNodeName, UserId userId) {
        return treeService.addNode(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(parentNodeId),
                NodeName.of(newNodeName),
                userId
        ).then();
    }

    public Mono<Void> removeNode(String treeId, long version, String nodeId, UserId userId) {
        return treeService.removeNode(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(nodeId),
                userId
        ).then();
    }

    public Mono<Void> swapNodes(String treeId, long version, String nodeId1, String nodeId2, UserId userId) {
        return treeService.swapNodes(
                TreeId.of(treeId),
                Version.of(version),
                NodeId.of(nodeId1),
                NodeId.of(nodeId2),
                userId
        ).then();
    }

}
