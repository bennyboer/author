package de.bennyboer.author.server.shared;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.permissions.Action;
import de.bennyboer.author.permissions.Permission;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.App;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.Profile;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessageListener;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.server.shared.websocket.api.SubscribeMessage;
import de.bennyboer.author.server.shared.websocket.api.SubscribeToPermissionsMessage;
import de.bennyboer.author.server.shared.websocket.api.WebSocketMessage;
import de.bennyboer.author.server.shared.websocket.api.WebSocketMessageMethod;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import io.javalin.testtools.HttpClient;
import lombok.Getter;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        RepoFactory.setTestingProfile(true);

        var builder = AppConfig.builder()
                .profile(Profile.TESTING);
        AppConfig config = configure(builder);

        App app = new App(config);
        messaging = app.getMessaging();
        jsonMapper = app.getJsonMapper();
        javalin = app.createJavalin();
    }

    protected abstract AppConfig configure(AppConfig.AppConfigBuilder configBuilder);

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
        Destination destination = messaging.getTopic(AggregateType.of(message.getAggregateType()));

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

    protected CountDownLatch getLatchForAwaitingEventOverWebSocket(
            HttpClient client,
            String token,
            AggregateType aggregateType,
            AggregateId aggregateId,
            EventName eventName
    ) throws InterruptedException {
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch eventReceived = new CountDownLatch(1);

        client.getOkHttp().newWebSocket(
                new Request.Builder().url("ws://localhost:%d/ws".formatted(javalin.port())).build(),
                new WebSocketListener() {
                    @Override
                    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                        WebSocketMessage msg = WebSocketMessage.builder()
                                .method(WebSocketMessageMethod.SUBSCRIBE)
                                .token(token)
                                .subscribe(SubscribeMessage.builder()
                                        .aggregateType(aggregateType.getValue())
                                        .aggregateId(aggregateId.getValue())
                                        .eventName(eventName.getValue())
                                        .build())
                                .build();
                        String jsonMsg = getJsonMapper().toJsonString(msg, WebSocketMessage.class);

                        webSocket.send(jsonMsg);

                        connected.countDown();
                    }

                    @Override
                    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                        WebSocketMessage msg = getJsonMapper().fromJsonString(text, WebSocketMessage.class);

                        msg.getEvent().ifPresent(eventMessage -> {
                            eventReceived.countDown();
                            webSocket.close(1000, "Test finished");
                        });
                    }
                }
        );

        boolean zeroed = connected.await(5, TimeUnit.SECONDS);
        if (!zeroed) {
            throw new RuntimeException("Timed out waiting for websocket connection");
        }

        return eventReceived;
    }

    protected CountDownLatch getLatchForAwaitingPermissionEventOverWebSocket(
            HttpClient client,
            String token,
            AggregateType aggregateType,
            @Nullable AggregateId aggregateId,
            @Nullable Action action
    ) throws InterruptedException {
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch eventReceived = new CountDownLatch(1);

        client.getOkHttp().newWebSocket(
                new Request.Builder().url("ws://localhost:%d/ws".formatted(javalin.port())).build(),
                new WebSocketListener() {
                    @Override
                    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                        WebSocketMessage msg = WebSocketMessage.builder()
                                .method(WebSocketMessageMethod.SUBSCRIBE_TO_PERMISSIONS)
                                .token(token)
                                .subscribeToPermissions(SubscribeToPermissionsMessage.builder()
                                        .aggregateType(aggregateType.getValue())
                                        .aggregateId(Optional.ofNullable(aggregateId)
                                                .map(AggregateId::getValue)
                                                .orElse(null))
                                        .action(Optional.ofNullable(action).map(Action::getName).orElse(null))
                                        .build())
                                .build();
                        String jsonMsg = getJsonMapper().toJsonString(msg, WebSocketMessage.class);

                        webSocket.send(jsonMsg);

                        connected.countDown();
                    }

                    @Override
                    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                        WebSocketMessage msg = getJsonMapper().fromJsonString(text, WebSocketMessage.class);

                        msg.getPermissionEvent().ifPresent(permissionEventMessage -> {
                            eventReceived.countDown();
                            webSocket.close(1000, "Test finished");
                        });
                    }
                }
        );

        boolean zeroed = connected.await(5, TimeUnit.SECONDS);
        if (!zeroed) {
            throw new RuntimeException("Timed out waiting for websocket connection");
        }

        return eventReceived;
    }

}
