package de.bennyboer.author.server.websocket.subscriptions;

import de.bennyboer.author.server.websocket.SessionId;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.utils.collections.ConcurrentHashSet;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SubscriptionManager {

    private final Map<AggregateType, Map<AggregateId, Set<SessionId>>> subscriptions = new ConcurrentHashMap<>();

    private final Map<SessionId, Set<SubscriptionTarget>> targetsPerSession = new ConcurrentHashMap<>();

    public void subscribe(SubscriptionTarget target, SessionId sessionId) {
        findSubscribers(target).add(sessionId);
        targetsPerSession.computeIfAbsent(sessionId, key -> new ConcurrentHashSet<>()).add(target);
        log.info(
                "Subscribed session ID '{}' to target '{}'. Total targets subscribed: {}",
                sessionId.getValue(),
                target,
                targetsPerSession.get(sessionId)
        );
    }

    public void unsubscribe(SubscriptionTarget target, SessionId sessionId) {
        findSubscribers(target).remove(sessionId);
        Optional.ofNullable(targetsPerSession.get(sessionId))
                .ifPresent(targets -> targets.remove(target));
        log.info(
                "Unsubscribed session ID '{}' from target '{}'. Remaining targets subscribed: {}",
                sessionId.getValue(),
                target,
                targetsPerSession.get(sessionId)
        );
    }

    public void unsubscribeFromAllTargets(SessionId sessionId) {
        Optional.ofNullable(targetsPerSession.remove(sessionId))
                .ifPresent(targets -> targets.forEach(target -> unsubscribe(target, sessionId)));
        log.info(
                "Unsubscribed session ID '{}' from all targets. Total targets subscribed: {}",
                sessionId.getValue(),
                targetsPerSession.get(sessionId)
        );
    }

    public Set<SessionId> getSubscribers(SubscriptionTarget target) {
        return Collections.unmodifiableSet(findSubscribers(target));
    }

    private Set<SessionId> findSubscribers(SubscriptionTarget target) {
        return subscriptions.computeIfAbsent(target.getAggregateType(), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(target.getAggregateId(), key -> new ConcurrentHashSet<>());
    }

}
