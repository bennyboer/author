package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.author.server.shared.messaging.MessageListenerId;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.websocket.SessionId;
import org.apache.activemq.artemis.utils.collections.ConcurrentHashSet;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSubscriptionManager<T> implements SubscriptionManager<T> {

    private final Messaging messaging;

    private final Map<SessionId, Set<T>> eventTargetsPerSession = new ConcurrentHashMap<>();

    private final Set<T> eventSubscriptionTargets = new ConcurrentHashSet<>();

    private final Map<T, MessageListenerId> messageListenersPerEventSubscriptionTarget = new ConcurrentHashMap<>();

    protected abstract Set<SessionId> findSubscribers(T target);

    protected abstract MessageListenerId registerMessageListenerForTarget(T target, Messaging messaging);

    public AbstractSubscriptionManager(Messaging messaging) {
        this.messaging = messaging;
    }

    @Override
    public void subscribe(T target, SessionId sessionId) {
        findSubscribers(target).add(sessionId);
        eventTargetsPerSession.computeIfAbsent(sessionId, key -> new ConcurrentHashSet<>()).add(target);

        boolean added = eventSubscriptionTargets.add(target);
        if (added) {
            setupMessageListenerForEventTarget(target);
        }
    }

    @Override
    public void unsubscribe(T target, SessionId sessionId) {
        findSubscribers(target).remove(sessionId);
        Optional.ofNullable(eventTargetsPerSession.get(sessionId))
                .ifPresent(targets -> targets.remove(target));

        boolean removed = eventSubscriptionTargets.remove(target);
        if (removed) {
            cleanupMessageListenerForTarget(target);
        }
    }

    @Override
    public void unsubscribeFromAllTargets(SessionId sessionId) {
        Optional.ofNullable(eventTargetsPerSession.remove(sessionId))
                .ifPresent(targets -> targets.forEach(target -> unsubscribe(target, sessionId)));
    }

    @Override
    public Set<SessionId> getSubscribers(T target) {
        return Collections.unmodifiableSet(findSubscribers(target));
    }

    private void setupMessageListenerForEventTarget(T target) {
        MessageListenerId messageListenerId = registerMessageListenerForTarget(target, messaging);
        messageListenersPerEventSubscriptionTarget.put(target, messageListenerId);
    }

    private void cleanupMessageListenerForTarget(T target) {
        MessageListenerId messageListenerId = messageListenersPerEventSubscriptionTarget.remove(target);
        messaging.deregisterAggregateEventMessageListener(messageListenerId);
    }

}
