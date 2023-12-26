package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.author.server.shared.websocket.SessionId;

import java.util.Set;

public interface SubscriptionManager<T> {

    void subscribe(T target, SessionId sessionId);

    void unsubscribe(T target, SessionId sessionId);

    void unsubscribeFromAllTargets(SessionId sessionId);

    Set<SessionId> getSubscribers(T target);

}
