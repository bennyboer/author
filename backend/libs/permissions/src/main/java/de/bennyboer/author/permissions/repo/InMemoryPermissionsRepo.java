package de.bennyboer.author.permissions.repo;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.Action;
import de.bennyboer.author.permissions.Permission;
import de.bennyboer.author.permissions.Resource;
import de.bennyboer.author.permissions.ResourceType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * A permissions repository that stores permissions in-memory.
 * This is useful for testing purposes only as it features no persistence and is not particularly fast.
 */
public class InMemoryPermissionsRepo implements PermissionsRepo {

    private final Set<Permission> permissions = ConcurrentHashMap.newKeySet();

    @Override
    public Mono<Permission> insert(Permission permission) {
        return Mono.just(permission)
                .mapNotNull(p -> permissions.add(p) ? p : null);
    }

    @Override
    public Flux<Permission> insertAll(Collection<Permission> permissions) {
        return Flux.fromIterable(permissions)
                .flatMap(this::insert);
    }

    @Override
    public Mono<Boolean> hasPermission(Permission permission) {
        return Mono.fromCallable(() -> permissions.contains(permission));
    }

    @Override
    public Flux<Permission> findPermissionsByUserId(UserId userId) {
        return Flux.fromIterable(permissions)
                .filter(permission -> permission.getUserId().equals(userId));
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResourceType(UserId userId, ResourceType resourceType) {
        return findPermissionsByUserId(userId)
                .filter(permission -> permission.getResource().getType().equals(resourceType));
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResourceTypeAndAction(
            UserId userId,
            ResourceType resourceType,
            Action action
    ) {
        return findPermissionsByUserId(userId)
                .filter(permission -> permission.getResource().getType().equals(resourceType)
                        && permission.getAction().equals(action));
    }

    @Override
    public Flux<Permission> findPermissionsByUserIdAndResource(UserId userId, Resource resource) {
        return findPermissionsByUserId(userId)
                .filter(permission -> permission.getResource().equals(resource));
    }

    @Override
    public Flux<Permission> removeByUserId(UserId userId) {
        return removeBy(permission -> permission.getUserId().equals(userId));
    }

    @Override
    public Flux<Permission> removeByResource(Resource resource) {
        return removeBy(permission -> permission.getResource().equals(resource));
    }

    @Override
    public Flux<Permission> removeByUserIdAndResource(UserId userId, Resource resource) {
        return removeBy(permission -> permission.getUserId().equals(userId)
                && permission.getResource().equals(resource));
    }

    @Override
    public Mono<Permission> removeByPermission(Permission permission) {
        return Mono.just(permission)
                .mapNotNull(p -> permissions.remove(p) ? p : null);
    }

    private Flux<Permission> removeBy(Predicate<Permission> predicate) {
        return Mono.fromCallable(() -> {
            List<Permission> removed = new ArrayList<>();

            for (Permission permission : permissions) {
                if (predicate.test(permission)) {
                    removed.add(permission);
                }
            }

            permissions.removeAll(removed);

            return removed;
        }).flatMapMany(Flux::fromIterable);
    }

}
