package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class SubscriptionTarget {

    AggregateType aggregateType;

    AggregateId aggregateId;

    public static SubscriptionTarget of(AggregateType aggregateType, AggregateId aggregateId) {
        checkNotNull(aggregateType, "aggregateType must not be null");
        checkNotNull(aggregateId, "aggregateId must not be null");

        return new SubscriptionTarget(aggregateType, aggregateId);
    }

    @Override
    public String toString() {
        return String.format("SubscriptionTarget(%s, %s)", aggregateType, aggregateId);
    }

}
