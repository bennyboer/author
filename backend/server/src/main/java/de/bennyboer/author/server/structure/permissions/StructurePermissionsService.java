package de.bennyboer.author.server.structure.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.shared.permissions.AggregatePermissionsService;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureId;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.author.server.structure.permissions.StructureAction.READ;

public class StructurePermissionsService extends AggregatePermissionsService<StructureId, StructureAction> {

    public StructurePermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        super(permissionsRepo, eventPublisher);
    }

    @Override
    public AggregateType getAggregateType() {
        return Structure.TYPE;
    }

    @Override
    public ResourceId getResourceId(StructureId id) {
        return ResourceId.of(id.getValue());
    }

    @Override
    public StructureId toId(ResourceId resourceId) {
        return StructureId.of(resourceId.getValue());
    }

    @Override
    public Action toAction(StructureAction action) {
        return Action.of(action.name());
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, StructureId structureId) {
        Resource resource = toResource(structureId);

        Set<Permission> permissions = Arrays.stream(StructureAction.values())
                .map(action -> Permission.builder()
                        .user(userId)
                        .isAllowedTo(toAction(action))
                        .on(resource))
                .collect(Collectors.toSet());

        return addPermissions(permissions);
    }

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return removePermissionsByUserId(userId);
    }

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, StructureId structureId) {
        return hasPermission(agent, READ, structureId);
    }

}
