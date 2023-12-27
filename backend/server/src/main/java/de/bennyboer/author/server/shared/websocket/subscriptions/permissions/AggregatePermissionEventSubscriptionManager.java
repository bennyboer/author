package de.bennyboer.author.server.shared.websocket.subscriptions.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.Action;
import de.bennyboer.author.server.shared.messaging.MessageListenerId;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessageListener;
import de.bennyboer.author.server.shared.websocket.SessionId;
import de.bennyboer.author.server.shared.websocket.subscriptions.EventPublishingSubscriptionManager;
import de.bennyboer.author.server.shared.websocket.subscriptions.SubscriberEventPublisher;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.utils.collections.ConcurrentHashSet;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AggregatePermissionEventSubscriptionManager
        extends EventPublishingSubscriptionManager<PermissionEventSubscriptionTarget, AggregatePermissionEventMessage> {

    private final Map<UserId, Map<AggregateType, Set<SessionId>>> userIdAndAggregateTypeSubscriptions =
            new ConcurrentHashMap<>();
    private final Map<UserId, Map<AggregateType, Map<Optional<AggregateId>, Set<SessionId>>>> userIdAndAggregateTypeAndIdSubscriptions =
            new ConcurrentHashMap<>();
    private final Map<UserId, Map<AggregateType, Map<Optional<Action>, Set<SessionId>>>> userIdAndAggregateTypeAndActionSubscriptions =
            new ConcurrentHashMap<>();
    private final Map<UserId, Map<AggregateType, Map<Optional<AggregateId>, Map<Optional<Action>, Set<SessionId>>>>> userIdAndAggregateTypeAndIdAndActionSubscriptions =
            new ConcurrentHashMap<>();

    public AggregatePermissionEventSubscriptionManager(
            Messaging messaging,
            SubscriberEventPublisher<AggregatePermissionEventMessage> eventPublisher
    ) {
        super(messaging, eventPublisher);
    }

    @Override
    protected void addSubscriber(PermissionEventSubscriptionTarget target, SessionId sessionId) {
        UserId userId = target.getUserId();
        AggregateType aggregateType = target.getAggregateType();
        Optional<AggregateId> aggregateId = target.getAggregateId();
        Optional<Action> action = target.getAction();

        findSubscribersOnUserIdAndAggregateType(userId, aggregateType).add(sessionId);
        findSubscribersOnUserIdAndAggregateTypeAndId(userId, aggregateType, aggregateId.orElse(null)).add(sessionId);
        findSubscribersOnUserIdAndAggregateTypeAndAction(userId, aggregateType, action.orElse(null)).add(sessionId);
        findSubscribersOnUserIdAndAggregateTypeAndIdAndAction(
                userId,
                aggregateType,
                aggregateId.orElse(null),
                action.orElse(null)
        ).add(
                sessionId
        );
    }

    @Override
    protected void removeSubscriber(PermissionEventSubscriptionTarget target, SessionId sessionId) {
        UserId userId = target.getUserId();
        AggregateType aggregateType = target.getAggregateType();
        Optional<AggregateId> aggregateId = target.getAggregateId();
        Optional<Action> action = target.getAction();

        findSubscribersOnUserIdAndAggregateType(userId, aggregateType).remove(sessionId);
        findSubscribersOnUserIdAndAggregateTypeAndId(userId, aggregateType, aggregateId.orElse(null)).remove(sessionId);
        findSubscribersOnUserIdAndAggregateTypeAndAction(userId, aggregateType, action.orElse(null)).remove(sessionId);
        findSubscribersOnUserIdAndAggregateTypeAndIdAndAction(
                userId,
                aggregateType,
                aggregateId.orElse(null),
                action.orElse(null)
        ).remove(
                sessionId
        );
    }

    @Override
    protected Set<SessionId> findSubscribers(PermissionEventSubscriptionTarget target) {
        UserId userId = target.getUserId();
        AggregateType aggregateType = target.getAggregateType();
        Optional<AggregateId> aggregateId = target.getAggregateId();
        Optional<Action> action = target.getAction();

        Set<SessionId> subscribers = new HashSet<>();

        subscribers.addAll(findSubscribersOnUserIdAndAggregateType(userId, aggregateType));
        subscribers.addAll(findSubscribersOnUserIdAndAggregateTypeAndId(
                userId,
                aggregateType,
                aggregateId.orElse(null)
        ));
        subscribers.addAll(findSubscribersOnUserIdAndAggregateTypeAndAction(
                userId,
                aggregateType,
                action.orElse(null)
        ));
        subscribers.addAll(findSubscribersOnUserIdAndAggregateTypeAndIdAndAction(
                userId,
                aggregateType,
                aggregateId.orElse(null),
                action.orElse(null)
        ));

        return subscribers;
    }

    @Override
    protected MessageListenerId registerMessageListenerForTarget(
            PermissionEventSubscriptionTarget target,
            Messaging messaging
    ) {
        return messaging.registerAggregatePermissionEventMessageListener(new AggregatePermissionEventMessageListener() {
            @Override
            public AggregateType aggregateType() {
                return target.getAggregateType();
            }

            @Override
            public Optional<AggregateId> aggregateId() {
                return target.getAggregateId();
            }

            @Override
            public Optional<Action> action() {
                return target.getAction();
            }

            @Override
            public Optional<UserId> userId() {
                return Optional.of(target.getUserId());
            }

            @Override
            public Mono<Void> onMessage(AggregatePermissionEventMessage message) {
                return Mono.fromRunnable(() -> publishEvent(message));
            }
        });
    }

    @Override
    protected PermissionEventSubscriptionTarget getTargetFromMessage(AggregatePermissionEventMessage msg) {
        return PermissionEventSubscriptionTarget.of(
                UserId.of(msg.getUserId()),
                AggregateType.of(msg.getAggregateType()),
                msg.getAggregateId().map(AggregateId::of).orElse(null),
                Action.of(msg.getAction())
        );
    }

    private Set<SessionId> findSubscribersOnUserIdAndAggregateType(UserId userId, AggregateType aggregateType) {
        return userIdAndAggregateTypeSubscriptions.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(aggregateType, key -> new ConcurrentHashSet<>());
    }

    private Set<SessionId> findSubscribersOnUserIdAndAggregateTypeAndId(
            UserId userId,
            AggregateType aggregateType,
            @Nullable AggregateId aggregateId
    ) {
        return userIdAndAggregateTypeAndIdSubscriptions.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(aggregateType, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(Optional.ofNullable(aggregateId), key -> new ConcurrentHashSet<>());
    }

    private Set<SessionId> findSubscribersOnUserIdAndAggregateTypeAndAction(
            UserId userId,
            AggregateType aggregateType,
            @Nullable Action action
    ) {
        return userIdAndAggregateTypeAndActionSubscriptions.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(aggregateType, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(Optional.ofNullable(action), key -> new ConcurrentHashSet<>());
    }

    private Set<SessionId> findSubscribersOnUserIdAndAggregateTypeAndIdAndAction(
            UserId userId,
            AggregateType aggregateType,
            @Nullable AggregateId aggregateId,
            @Nullable Action action
    ) {
        return userIdAndAggregateTypeAndIdAndActionSubscriptions.computeIfAbsent(
                        userId,
                        key -> new ConcurrentHashMap<>()
                )
                .computeIfAbsent(aggregateType, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(Optional.ofNullable(aggregateId), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(Optional.ofNullable(action), key -> new ConcurrentHashSet<>());
    }

}
