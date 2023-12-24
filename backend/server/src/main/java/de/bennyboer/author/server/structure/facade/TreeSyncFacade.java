package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.structure.tree.TreeService;
import de.bennyboer.author.structure.tree.nodes.NodeName;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class TreeSyncFacade {

    TreeService treeService;

    public Mono<Void> create(String projectId, UserId userId) {
        // TODO Load project to get project name to use for the root nodes name
        NodeName rootNodeName = NodeName.of("Root");

        return treeService.create(projectId, rootNodeName, Agent.user(userId)).then();
    }

}
