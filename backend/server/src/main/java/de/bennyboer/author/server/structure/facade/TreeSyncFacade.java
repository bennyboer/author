package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.persistence.lookup.TreeLookupRepo;
import de.bennyboer.author.structure.tree.TreeId;
import de.bennyboer.author.structure.tree.TreeService;
import de.bennyboer.author.structure.tree.nodes.NodeName;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class TreeSyncFacade {

    TreeService treeService;

    TreeLookupRepo lookupRepo;

    public Mono<Void> create(String projectId, UserId userId) {
        // TODO Load project to get project name to use for the root nodes name
        NodeName rootNodeName = NodeName.of("Root");

        return treeService.create(projectId, rootNodeName, Agent.user(userId)).then();
    }

    public Mono<Void> removeTreeByProjectId(String projectId, Agent agent) {
        return lookupRepo.findTreeIdByProjectId(projectId)
                .flatMap(treeId -> treeService.remove(treeId, agent))
                .then();
    }

    public Mono<Void> addToLookup(TreeId treeId) {
        return treeService.get(treeId)
                .flatMap(lookupRepo::update)
                .then();
    }

    public Mono<Void> removeFromLookup(TreeId treeId) {
        return lookupRepo.remove(treeId);
    }

}
