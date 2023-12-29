package de.bennyboer.author.server.shared.websocket.api;

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
public class SubscribeMessage {

    String aggregateType;

    String aggregateId;

    @Nullable
    String eventName;

    public static SubscribeMessage of(
            String aggregateType,
            String aggregateId,
            @Nullable String eventName
    ) {
        checkNotNull(aggregateType, "Aggregate type must be given");

        return new SubscribeMessage(aggregateType, aggregateId, eventName);
    }

    public AggregateType getAggregateType() {
        return AggregateType.of(aggregateType);
    }

    public AggregateId getAggregateId() {
        return AggregateId.of(aggregateId);
    }

    public Optional<EventName> getEventName() {
        return Optional.ofNullable(eventName).map(EventName::of);
    }

}
