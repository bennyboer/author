package de.bennyboer.author.server.shared;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.Permission;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessageListener;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class ModuleTest {

    protected abstract Messaging getMessaging();

    protected void awaitPermissionCreation(Permission permission, PermissionsRepo repo) {
        awaitPermissionCreation(permission, repo, Duration.ofSeconds(5));
    }

    /**
     * Wait until the permission is created in the repo.
     * If the permission is already present, the method returns immediately.
     */
    protected void awaitPermissionCreation(Permission permission, PermissionsRepo repo, Duration timeout) {
        Messaging messaging = getMessaging();
        CountDownLatch latch = new CountDownLatch(1);

        AggregateType aggregateType = AggregateType.of(permission.getResource().getType().getName());
        UserId userId = permission.getUserId();

        messaging.registerAggregatePermissionEventMessageListener(new AggregatePermissionEventMessageListener() {

            @Override
            public AggregateType aggregateType() {
                return aggregateType;
            }

            @Override
            public Optional<UserId> userId() {
                return Optional.of(userId);
            }

            @Override
            public Mono<Void> onMessage(AggregatePermissionEventMessage message) {
                return repo.hasPermission(permission)
                        .doOnNext(hasPermission -> {
                            if (hasPermission) {
                                latch.countDown();
                            }
                        })
                        .then();
            }

        });

        boolean hasPermission = repo.hasPermission(permission).block();
        if (hasPermission) {
            latch.countDown();
        }

        try {
            boolean zeroReached = latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
            if (!zeroReached) {
                throw new RuntimeException("Timed out waiting for permission creation: %s".formatted(permission));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void awaitPermissionRemoval(Permission permission, PermissionsRepo repo) {
        awaitPermissionRemoval(permission, repo, Duration.ofSeconds(5));
    }

    /**
     * Wait until the permission is removed from the repo.
     * If the permission is already removed, the method returns immediately.
     */
    protected void awaitPermissionRemoval(Permission permission, PermissionsRepo repo, Duration timeout) {
        Messaging messaging = getMessaging();
        CountDownLatch latch = new CountDownLatch(1);

        AggregateType aggregateType = AggregateType.of(permission.getResource().getType().getName());
        UserId userId = permission.getUserId();

        messaging.registerAggregatePermissionEventMessageListener(new AggregatePermissionEventMessageListener() {

            @Override
            public AggregateType aggregateType() {
                return aggregateType;
            }

            @Override
            public Optional<UserId> userId() {
                return Optional.of(userId);
            }

            @Override
            public Mono<Void> onMessage(AggregatePermissionEventMessage message) {
                return repo.hasPermission(permission)
                        .doOnNext(hasPermission -> {
                            if (!hasPermission) {
                                latch.countDown();
                            }
                        })
                        .then();
            }

        });

        boolean hasPermission = repo.hasPermission(permission).block();
        if (!hasPermission) {
            latch.countDown();
        }

        try {
            boolean zeroReached = latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
            if (!zeroReached) {
                throw new RuntimeException("Timed out waiting for permission creation: %s".formatted(permission));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO Method to easily publish aggregate event message

}
