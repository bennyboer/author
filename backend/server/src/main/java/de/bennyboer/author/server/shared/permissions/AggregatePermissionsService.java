package de.bennyboer.author.server.shared.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import reactor.core.publisher.Mono;

import java.util.Set;

public abstract class AggregatePermissionsService<ID, A> {

    private final PermissionsService permissionsService;

    public AggregatePermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        this.permissionsService = new PermissionsService(permissionsRepo, eventPublisher);
    }

    public abstract AggregateType getAggregateType();

    public abstract ResourceId getResourceId(ID id);

    public abstract Action toAction(A action);

    public Resource toResource(ID id) {
        return Resource.of(getResourceType(), getResourceId(id));
    }

    protected ResourceType getResourceType() {
        return ResourceType.of(getAggregateType().getValue());
    }

    protected Mono<Void> addPermissions(Set<Permission> permissions) {
        return permissionsService.addPermissions(permissions);
    }

    protected Mono<Void> removePermissionsByUserId(UserId userId) {
        return permissionsService.removePermissionsByUserId(userId);
    }

    protected Mono<Void> removePermissionsByResource(Resource resource) {
        return permissionsService.removePermissionsByResource(resource);
    }

}
