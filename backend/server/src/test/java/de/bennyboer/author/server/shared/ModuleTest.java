package de.bennyboer.author.server.shared;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.Permission;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.App;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessageListener;
import de.bennyboer.author.user.User;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import lombok.Getter;
import reactor.core.publisher.Mono;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class ModuleTest {

    @Getter
    private final Messaging messaging;

    @Getter
    private final JsonMapper jsonMapper;

    @Getter
    private final Javalin javalin;

    public ModuleTest() {
        AppConfig config = configure();

        App app = new App(config);
        messaging = app.getMessaging();
        jsonMapper = app.getJsonMapper();
        javalin = app.createJavalin();
    }

    protected abstract AppConfig configure();

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

    protected void publishAggregateEventMessage(AggregateEventMessage message) {
        Messaging messaging = getMessaging();
        JsonMapper jsonMapper = getJsonMapper();

        JMSProducer producer = messaging.getContext().createProducer();
        Destination destination = messaging.getTopic(User.TYPE);

        String json = jsonMapper.toJsonString(message, AggregateEventMessage.class);
        TextMessage textMessage = messaging.getContext().createTextMessage(json);
        try {
            textMessage.setStringProperty("aggregateId", message.getAggregateId());
            textMessage.setStringProperty("eventName", message.getEventName());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        producer.send(destination, textMessage);
    }

}
