package de.bennyboer.author.server.shared.messaging.events;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AggregateEventMessageListener {

    AggregateType aggregateType();

    default Optional<AggregateId> aggregateId() {
        return Optional.empty();
    }

    default Optional<EventName> eventName() {
        return Optional.empty();
    }

    Mono<Void> onMessage(AggregateEventMessage message);

}
