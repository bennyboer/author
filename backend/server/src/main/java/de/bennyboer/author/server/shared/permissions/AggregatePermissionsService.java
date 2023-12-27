package de.bennyboer.author.server.shared.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

public abstract class AggregatePermissionsService<ID, A> {

    private final PermissionsService permissionsService;

    public AggregatePermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        this.permissionsService = new PermissionsService(permissionsRepo, eventPublisher);
    }

    public abstract AggregateType getAggregateType();

    public abstract ResourceId getResourceId(ID id);

    public abstract ID toId(ResourceId resourceId);

    public abstract Action toAction(A action);

    public Resource toResource(ID id) {
        return Resource.of(getResourceType(), getResourceId(id));
    }

    public Mono<Void> assertHasPermission(Agent agent, A action, ID id) {
        return assertHasPermission(agent, toAction(action), toResource(id));
    }

    public Mono<Void> assertHasPermission(Agent agent, A action) {
        return assertHasPermission(agent, toAction(action), Resource.ofType(getResourceType()));
    }

    public Mono<Boolean> hasPermission(Agent agent, A action, ID id) {
        return hasPermission(agent, toAction(action), toResource(id));
    }

    public Mono<Boolean> hasPermission(Agent agent, A action) {
        return hasPermission(agent, toAction(action), Resource.ofType(getResourceType()));
    }

    public Mono<Void> removePermissionsByResource(ID id) {
        return permissionsService.removePermissionsByResource(toResource(id));
    }

    public Flux<ID> getAccessibleResourceIds(Agent agent, A action) {
        return agent.getUserId()
                .map(userId -> permissionsService.findPermissionsByUserIdAndResourceTypeAndAction(
                                userId,
                                getResourceType(),
                                toAction(action)
                        )
                        .mapNotNull(permission -> toNullableId(permission.getResource().getId().orElse(null))))
                .orElse(Flux.empty());
    }

    protected ResourceType getResourceType() {
        return ResourceType.of(getAggregateType().getValue());
    }

    protected Mono<Void> addPermission(Permission permission) {
        return permissionsService.addPermission(permission);
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

    protected Mono<Void> assertHasPermission(Agent agent, Action action, Resource resource) {
        return hasPermission(agent, action, resource)
                .flatMap(hasPermission -> {
                    if (hasPermission) {
                        return Mono.empty();
                    }

                    return Mono.error(new MissingPermissionException(Permission.builder()
                            .user(agent.getUserId().orElse(UserId.of("ANONYMOUS")))
                            .isAllowedTo(action)
                            .on(resource)));
                });
    }

    protected Mono<Boolean> hasPermission(Agent agent, Action action, Resource resource) {
        if (agent.isSystem()) {
            return Mono.just(true);
        }

        UserId userId = agent.getUserId().orElse(UserId.of("ANONYMOUS"));
        var permission = Permission.builder()
                .user(userId)
                .isAllowedTo(action)
                .on(resource);

        return permissionsService.hasPermission(permission);
    }

    private ID toNullableId(@Nullable ResourceId resourceId) {
        return Optional.ofNullable(resourceId)
                .map(this::toId)
                .orElse(null);
    }

}
