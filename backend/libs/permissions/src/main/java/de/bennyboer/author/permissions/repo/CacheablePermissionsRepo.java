package de.bennyboer.author.permissions.repo;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.Permission;
import de.bennyboer.author.permissions.Resource;
import de.bennyboer.author.permissions.ResourceType;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Permissions-Queries are putting a lot of load on the database as they are called frequently.
 * Generally, every call to the backend needs to check permissions before processing the request!
 * This cache is intended to reduce the load by caching a number of last-needed permissions in-memory.
 */
@AllArgsConstructor
public class CacheablePermissionsRepo implements PermissionsRepo {

    private final PermissionsRepo delegate;

    @Override
    public Mono<Void> insert(Permission permission) {
        return delegate.insert(permission);
    }

    @Override
    public Mono<Void> insertAll(Collection<Permission> permissions) {
        return delegate.insertAll(permissions);
    }

    @Override
    public Mono<Boolean> hasPermission(Permission permission) {
        return delegate.hasPermission(permission); // TODO This is to be cached!
    }

    @Override
    public Flux<Permission> findPermissionsByUserId(UserId userId) {
        return delegate.findPermissionsByUserId(userId);
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResourceType(UserId userId, ResourceType resourceType) {
        return delegate.findPermissionsByUserIdAndResourceType(userId, resourceType);
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResource(UserId userId, Resource resource) {
        return delegate.findPermissionsByUserIdAndResource(userId, resource);
    }

    @Override
    public Flux<Permission> removeByUserId(UserId userId) {
        return delegate.removeByUserId(userId);
    }

    @Override
    public Flux<Permission> removeByResource(Resource resource) {
        return delegate.removeByResource(resource);
    }

    @Override
    public Flux<Permission> removeByUserIdAndResource(UserId userId, Resource resource) {
        return delegate.removeByUserIdAndResource(userId, resource);
    }

    @Override
    public Mono<Void> removeByPermission(Permission permission) {
        return delegate.removeByPermission(permission);
    }

}
