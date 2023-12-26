package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.author.server.shared.websocket.SessionId;

import java.util.Set;

public interface SubscriberEventPublisher<M> {

    void publishEvent(M msg, Set<SessionId> subscribers);

}
