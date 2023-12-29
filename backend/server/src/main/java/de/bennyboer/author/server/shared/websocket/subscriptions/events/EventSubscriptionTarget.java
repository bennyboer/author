package de.bennyboer.author.server.shared.websocket.subscriptions.events;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class EventSubscriptionTarget {

    AggregateType aggregateType;

    AggregateId aggregateId;

    @Nullable
    EventName eventName;

    public static EventSubscriptionTarget of(
            AggregateType aggregateType,
            AggregateId aggregateId,
            @Nullable EventName eventName
    ) {
        checkNotNull(aggregateType, "Aggregate type must be given");

        return new EventSubscriptionTarget(aggregateType, aggregateId, eventName);
    }

    public Optional<EventName> getEventName() {
        return Optional.ofNullable(eventName);
    }

    @Override
    public String toString() {
        return String.format("EventSubscriptionTarget(%s, %s, %s)", aggregateType, aggregateId, eventName);
    }

}
