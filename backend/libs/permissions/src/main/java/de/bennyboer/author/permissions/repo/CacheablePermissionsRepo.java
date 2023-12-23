package de.bennyboer.author.permissions.repo;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.Permission;
import de.bennyboer.author.permissions.Resource;
import de.bennyboer.author.permissions.ResourceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;

/**
 * Permissions-Queries are putting a lot of load on the database as they are called frequently.
 * Generally, every call to the backend needs to check permissions before processing the request!
 * This cache is intended to reduce the load by caching a number of last-needed permissions in-memory.
 */
public class CacheablePermissionsRepo implements PermissionsRepo {

    private final PermissionsRepo delegate;
    private final Cache<Permission, Boolean> cache;

    public CacheablePermissionsRepo(PermissionsRepo delegate, Config config) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .maximumSize(config.getMaximumSize())
                .expireAfterWrite(config.getExpireAfterWrite())
                .build();
    }

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
        return Mono.justOrEmpty(cache.getIfPresent(permission))
                .switchIfEmpty(delegate.hasPermission(permission)
                        .doOnNext(hasPermission -> cache.put(permission, hasPermission)));
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

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class Config {

        long maximumSize;

        /**
         * A short duration is recommended to avoid stale date.
         * For example when permissions are taken away from a user, the cache should not keep the old permissions for
         * too long.
         */
        Duration expireAfterWrite;

    }

}
