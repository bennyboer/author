package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.structure.permissions.StructurePermissionsService;
import de.bennyboer.author.structure.StructureId;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class StructurePermissionsFacade {

    StructurePermissionsService permissionsService;

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return permissionsService.removePermissionsForUser(userId);
    }

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, StructureId structureId) {
        return permissionsService.hasPermissionToReceiveEvents(agent, structureId);
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, StructureId structureId) {
        return permissionsService.addPermissionsForCreator(userId, structureId);
    }

    public Mono<Void> removePermissionsForStructure(StructureId structureId) {
        return permissionsService.removePermissionsByResource(structureId);
    }

}
