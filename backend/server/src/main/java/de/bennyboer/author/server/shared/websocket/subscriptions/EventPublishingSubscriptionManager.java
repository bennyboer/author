package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.author.server.shared.messaging.Messaging;

/**
 * Subscription manager that will publish events to subscribers.
 *
 * @param <T> The type of the event target
 * @param <M> The type of the event message
 */
public abstract class EventPublishingSubscriptionManager<T, M> extends AbstractSubscriptionManager<T> {

    private final SubscriberEventPublisher<M> eventPublisher;

    public EventPublishingSubscriptionManager(Messaging messaging, SubscriberEventPublisher<M> eventPublisher) {
        super(messaging);
        this.eventPublisher = eventPublisher;
    }

    protected void publishEvent(M msg) {
        T target = getTargetFromMessage(msg);
        eventPublisher.publishEvent(msg, getSubscribers(target));
    }

    protected abstract T getTargetFromMessage(M msg);

}
