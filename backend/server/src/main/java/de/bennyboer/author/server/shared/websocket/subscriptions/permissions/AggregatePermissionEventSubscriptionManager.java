package de.bennyboer.author.server.shared.websocket.subscriptions.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.server.shared.messaging.MessageListenerId;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessageListener;
import de.bennyboer.author.server.shared.websocket.SessionId;
import de.bennyboer.author.server.shared.websocket.subscriptions.EventPublishingSubscriptionManager;
import de.bennyboer.author.server.shared.websocket.subscriptions.SubscriberEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.utils.collections.ConcurrentHashSet;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AggregatePermissionEventSubscriptionManager
        extends EventPublishingSubscriptionManager<PermissionEventSubscriptionTarget, AggregatePermissionEventMessage> {

    private final Map<AggregateType, Map<Optional<AggregateId>, Map<Optional<UserId>, Set<SessionId>>>> permissionEventSubscriptions =
            new ConcurrentHashMap<>();

    public AggregatePermissionEventSubscriptionManager(
            Messaging messaging,
            SubscriberEventPublisher<AggregatePermissionEventMessage> eventPublisher
    ) {
        super(messaging, eventPublisher);
    }

    @Override
    protected Set<SessionId> findSubscribers(PermissionEventSubscriptionTarget target) {
        AggregateType aggregateType = target.getAggregateType();
        Optional<AggregateId> aggregateId = target.getAggregateId();
        Optional<UserId> userId = target.getUserId();

        return permissionEventSubscriptions.computeIfAbsent(aggregateType, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(aggregateId, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, key -> new ConcurrentHashSet<>());
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
            public Optional<UserId> userId() {
                return target.getUserId();
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
                AggregateType.of(msg.getAggregateType()),
                msg.getAggregateId().map(AggregateId::of).orElse(null),
                UserId.of(msg.getUserId())
        );
    }

}
