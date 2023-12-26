package de.bennyboer.author.server.shared.websocket.api;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
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
public class UnsubscribeFromPermissionsMessage {

    String aggregateType;

    @Nullable
    String aggregateId;

    public static UnsubscribeFromPermissionsMessage of(
            String aggregateType,
            @Nullable String aggregateId
    ) {
        checkNotNull(aggregateType, "Aggregate type must not be null");

        return new UnsubscribeFromPermissionsMessage(aggregateType, aggregateId);
    }

    public AggregateType getAggregateType() {
        return AggregateType.of(aggregateType);
    }

    public Optional<AggregateId> getAggregateId() {
        return Optional.ofNullable(aggregateId).map(AggregateId::of);
    }

}
