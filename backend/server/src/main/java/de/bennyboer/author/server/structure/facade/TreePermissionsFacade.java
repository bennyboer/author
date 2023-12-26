package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.permissions.TreePermissionsService;
import de.bennyboer.author.structure.tree.TreeId;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class TreePermissionsFacade {

    TreePermissionsService permissionsService;

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return permissionsService.removePermissionsForUser(userId);
    }

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, TreeId treeId) {
        return permissionsService.hasPermissionToReceiveEvents(agent, treeId);
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, TreeId treeId) {
        return permissionsService.addPermissionsForCreator(userId, treeId);
    }

}
