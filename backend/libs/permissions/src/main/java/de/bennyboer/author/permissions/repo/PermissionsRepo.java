package de.bennyboer.author.permissions.repo;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.Permission;
import de.bennyboer.author.permissions.Resource;
import de.bennyboer.author.permissions.ResourceType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Data-Access interface for permissions.
 * It is intended to store and retrieve permissions from a persistent storage.
 */
public interface PermissionsRepo {

    Mono<Void> insert(Permission permission);

    Mono<Void> insertAll(Collection<Permission> permissions);

    Mono<Boolean> hasPermission(Permission permission);

    /**
     * Find all permissions for a specific user.
     * This may be useful if you want to display all permissions for a user.
     */
    Flux<Permission> findPermissionsByUserId(UserId userId);

    /**
     * Find all permissions for a specific user and resource type.
     * This may be useful if you want to display all permissions for a user on a specific resource type.
     * For example if you want to query all accessible projects for a user.
     */
    Flux<Permission> findPermissionsByUserIdAndResourceType(UserId userId, ResourceType resourceType);

    /**
     * Find all permissions for a specific resource.
     * This may be useful if you want to display all permissions to a resource for a user.
     */
    Flux<Permission> findPermissionsByUserIdAndResource(UserId userId, Resource resource);

    /**
     * Remove all permissions for a specific user.
     * This is useful when a user is deleted and all permissions should be removed.
     */
    Flux<Permission> removeByUserId(UserId userId);

    /**
     * Remove all permissions for a specific resource.
     * This is useful when a resource is deleted and all permissions to it should be removed.
     */
    Flux<Permission> removeByResource(Resource resource);

    /**
     * Remove all permissions for a specific user and resource.
     * This is useful when all permissions to a resource should be removed for a user.
     */
    Flux<Permission> removeByUserIdAndResource(UserId userId, Resource resource);

    /**
     * Remove a specific permission.
     */
    Mono<Void> removeByPermission(Permission permission);

}
