package de.bennyboer.author.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.event.PermissionEvent;
import de.bennyboer.author.permissions.repo.CacheablePermissionsRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionsService {

    private final PermissionsRepo permissionsRepo;
    private final PermissionsEventPublisher eventPublisher;

    public PermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        this.permissionsRepo = new CacheablePermissionsRepo(
                permissionsRepo,
                CacheablePermissionsRepo.Config.builder()
                        .maximumSize(10_000)
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build()
        );
        this.eventPublisher = eventPublisher;
    }

    public Mono<Void> addPermission(Permission permission) {
        return permissionsRepo.insert(permission)
                .flatMap(addedPermission -> eventPublisher.publish(PermissionEvent.added(addedPermission)));
    }

    public Mono<Void> addPermissions(Set<Permission> permissions) {
        return permissionsRepo.insertAll(permissions)
                .collect(Collectors.toSet())
                .flatMap(addedPermissions -> eventPublisher.publish(PermissionEvent.added(addedPermissions)));
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

    public Flux<Permission> findPermissionsByUserIdAndResourceTypeAndAction(
            UserId userId,
            ResourceType resourceType,
            Action action
    ) {
        return permissionsRepo.findPermissionsByUserIdAndResourceTypeAndAction(userId, resourceType, action);
    }

    public Mono<Void> removePermission(Permission permission) {
        return permissionsRepo.removeByPermission(permission)
                .flatMap(removedPermission -> eventPublisher.publish(PermissionEvent.removed(removedPermission)));
    }

    public Mono<Void> removePermissionsByUserId(UserId userId) {
        return permissionsRepo.removeByUserId(userId)
                .collect(Collectors.toSet())
                .flatMap(permissions -> eventPublisher.publish(PermissionEvent.removed(permissions)));
    }

    public Mono<Void> removePermissionsByResource(Resource resource) {
        return permissionsRepo.removeByResource(resource)
                .collect(Collectors.toSet())
                .flatMap(permissions -> eventPublisher.publish(PermissionEvent.removed(permissions)));
    }

    public Mono<Void> removePermissionsByUserIdAndResource(UserId userId, Resource resource) {
        return permissionsRepo.removeByUserIdAndResource(userId, resource)
                .collect(Collectors.toSet())
                .flatMap(permissions -> eventPublisher.publish(PermissionEvent.removed(permissions)));
    }

}
