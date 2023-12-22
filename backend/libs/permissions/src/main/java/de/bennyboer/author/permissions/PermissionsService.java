package de.bennyboer.author.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.event.PermissionEvent;
import de.bennyboer.author.permissions.repo.CacheablePermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class PermissionsService {

    private final PermissionsRepo permissionsRepo;
    private final PermissionsEventPublisher eventPublisher;

    public PermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        this.permissionsRepo = new CacheablePermissionsRepo(permissionsRepo);
        this.eventPublisher = eventPublisher;
    }

    public Mono<Void> addPermission(Permission permission) {
        return permissionsRepo.insert(permission)
                .then(eventPublisher.publish(PermissionEvent.added(permission)));
    }

    public Mono<Void> addPermissions(Collection<Permission> permissions) {
        return permissionsRepo.insertAll(permissions)
                .then(eventPublisher.publish(PermissionEvent.added(permissions)));
    }

    public Mono<Boolean> hasPermission(Permission permission) {
        return permissionsRepo.hasPermission(permission);
    }

    public Flux<Permission> findPermissionsByUserId(UserId userId) {
        return permissionsRepo.findPermissionsByUserId(userId);
    }

    public Flux<Permission> findPermissionsByUserIdAndResourceType(UserId userId, ResourceType resourceType) {
        return permissionsRepo.findPermissionsByUserIdAndResourceType(userId, resourceType);
    }

    public Flux<Permission> findPermissionsByUserIdAndResource(UserId userId, Resource resource) {
        return permissionsRepo.findPermissionsByUserIdAndResource(userId, resource);
    }

    public Mono<Void> removePermission(Permission permission) {
        return permissionsRepo.removeByPermission(permission)
                .then(eventPublisher.publish(PermissionEvent.removed(permission)));
    }

    public Mono<Void> removePermissionsByUserId(UserId userId) {
        return permissionsRepo.removeByUserId(userId)
                .collectList()
                .flatMap(permissions -> eventPublisher.publish(PermissionEvent.removed(permissions)));
    }

    public Mono<Void> removePermissionsByResource(Resource resource) {
        return permissionsRepo.removeByResource(resource)
                .collectList()
                .flatMap(permissions -> eventPublisher.publish(PermissionEvent.removed(permissions)));
    }

    public Mono<Void> removePermissionsByUserIdAndResource(UserId userId, Resource resource) {
        return permissionsRepo.removeByUserIdAndResource(userId, resource)
                .collectList()
                .flatMap(permissions -> eventPublisher.publish(PermissionEvent.removed(permissions)));
    }

}
