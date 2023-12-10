package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.messages.AggregateEventMessage;
import de.bennyboer.author.server.shared.websocket.SessionId;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.event.EventName;
import io.javalin.json.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.utils.collections.ConcurrentHashSet;

import javax.jms.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@AllArgsConstructor
public class SubscriptionManager {

    private final Messaging messaging;

    private final JsonMapper jsonMapper;

    private final SubscriptionEventListener subscriptionEventListener;

    private final Map<AggregateType, Map<AggregateId, Set<SessionId>>> subscriptions = new ConcurrentHashMap<>();

    private final Map<SessionId, Set<SubscriptionTarget>> targetsPerSession = new ConcurrentHashMap<>();

    private final Set<SubscriptionTarget> subscriptionTargets = new ConcurrentHashSet<>();

    private final Map<SubscriptionTarget, JMSConsumer> messageListenersPerTarget = new ConcurrentHashMap<>();

    public void subscribe(SubscriptionTarget target, SessionId sessionId) {
        findSubscribers(target).add(sessionId);
        targetsPerSession.computeIfAbsent(sessionId, key -> new ConcurrentHashSet<>()).add(target);

        boolean added = subscriptionTargets.add(target);
        if (added) {
            setupMessageListenerForTarget(target);
        }
    }

    public void unsubscribe(SubscriptionTarget target, SessionId sessionId) {
        findSubscribers(target).remove(sessionId);
        Optional.ofNullable(targetsPerSession.get(sessionId))
                .ifPresent(targets -> targets.remove(target));

        boolean removed = subscriptionTargets.remove(target);
        if (removed) {
            cleanupMessageListenerForTarget(target);
        }
    }

    public void unsubscribeFromAllTargets(SessionId sessionId) {
        Optional.ofNullable(targetsPerSession.remove(sessionId))
                .ifPresent(targets -> targets.forEach(target -> unsubscribe(target, sessionId)));
    }

    public Set<SessionId> getSubscribers(SubscriptionTarget target) {
        return Collections.unmodifiableSet(findSubscribers(target));
    }

    private Set<SessionId> findSubscribers(SubscriptionTarget target) {
        return subscriptions.computeIfAbsent(target.getAggregateType(), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(target.getAggregateId(), key -> new ConcurrentHashSet<>());
    }

    private void setupMessageListenerForTarget(SubscriptionTarget target) {
        JMSContext ctx = messaging.getContext();
        Topic topic = messaging.getTopic(target.getAggregateType());

        String aggregateIdMessageSelector = String.format("aggregateId = '%s'", target.getAggregateId().getValue());
        JMSConsumer consumer = ctx.createConsumer(topic, aggregateIdMessageSelector);
        consumer.setMessageListener(message -> parseAggregateEventMessage(message)
                .ifPresent(this::publishEventForAggregateEventMessage));

        messageListenersPerTarget.put(target, consumer);
    }

    private Optional<AggregateEventMessage> parseAggregateEventMessage(Message msg) {
        if (msg instanceof TextMessage textMessage) {
            try {
                String json = textMessage.getText();
                return Optional.of(jsonMapper.fromJsonString(json, AggregateEventMessage.class));
            } catch (Exception e) {
                log.error("Failed to parse AggregateEventMessage", e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private void publishEventForAggregateEventMessage(AggregateEventMessage msg) {
        AggregateType aggregateType = AggregateType.of(msg.getAggregateType());
        AggregateId aggregateId = AggregateId.of(msg.getAggregateId());
        Version version = Version.of(msg.getAggregateVersion());

        EventTopic topic = EventTopic.of(aggregateType, aggregateId, version);
        EventName eventName = EventName.of(msg.getEventName());
        Version eventVersion = Version.of(msg.getEventVersion());
        Map<String, Object> payload = msg.getPayload();

        subscriptionEventListener.onEvent(topic, eventName, eventVersion, payload);
    }

    private void cleanupMessageListenerForTarget(SubscriptionTarget target) {
        JMSConsumer consumer = messageListenersPerTarget.remove(target);
        consumer.close();
    }

}
