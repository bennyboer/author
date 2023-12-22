package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventTopic {

    AggregateType aggregateType;

    AggregateId aggregateId;

    Version version;

    public static EventTopic of(AggregateType aggregateType, AggregateId aggregateId, Version version) {
        checkNotNull(aggregateType, "aggregateType must not be null");
        checkNotNull(aggregateId, "aggregateId must not be null");
        checkNotNull(version, "version must not be null");

        return new EventTopic(aggregateType, aggregateId, version);
    }

    public SubscriptionTarget toSubscriptionTarget() {
        return SubscriptionTarget.of(aggregateType, aggregateId);
    }

}
