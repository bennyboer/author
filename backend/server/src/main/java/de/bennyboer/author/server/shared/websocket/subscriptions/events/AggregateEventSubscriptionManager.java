package de.bennyboer.author.server.shared.websocket.subscriptions.events;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.server.shared.messaging.MessageListenerId;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
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
public class AggregateEventSubscriptionManager
        extends EventPublishingSubscriptionManager<EventSubscriptionTarget, AggregateEventMessage> {

    private final Map<AggregateType, Map<AggregateId, Map<Optional<EventName>, Set<SessionId>>>> eventSubscriptions =
            new ConcurrentHashMap<>();

    public AggregateEventSubscriptionManager(
            Messaging messaging,
            SubscriberEventPublisher<AggregateEventMessage> eventPublisher
    ) {
        super(messaging, eventPublisher);
    }

    @Override
    protected Set<SessionId> findSubscribers(EventSubscriptionTarget target) {
        AggregateType aggregateType = target.getAggregateType();
        AggregateId aggregateId = target.getAggregateId();
        Optional<EventName> eventName = target.getEventName();

        return eventSubscriptions.computeIfAbsent(aggregateType, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(aggregateId, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(eventName, key -> new ConcurrentHashSet<>());
    }

    @Override
    protected MessageListenerId registerMessageListenerForTarget(EventSubscriptionTarget target, Messaging messaging) {
        return messaging.registerAggregateEventMessageListener(new AggregateEventMessageListener() {
            @Override
            public AggregateType aggregateType() {
                return target.getAggregateType();
            }

            @Override
            public Optional<AggregateId> aggregateId() {
                return Optional.of(target.getAggregateId());
            }

            @Override
            public Optional<EventName> eventName() {
                return target.getEventName();
            }

            @Override
            public Mono<Void> onMessage(AggregateEventMessage message) {
                return Mono.fromRunnable(() -> publishEvent(message));
            }
        });
    }

    @Override
    protected EventSubscriptionTarget getTargetFromMessage(AggregateEventMessage msg) {
        AggregateType aggregateType = AggregateType.of(msg.getAggregateType());
        AggregateId aggregateId = AggregateId.of(msg.getAggregateId());
        EventName eventName = EventName.of(msg.getEventName());

        return EventSubscriptionTarget.of(aggregateType, aggregateId, eventName);
    }

}
