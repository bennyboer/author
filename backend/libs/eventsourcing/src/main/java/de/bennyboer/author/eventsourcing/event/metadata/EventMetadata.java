package de.bennyboer.eventsourcing.event.metadata;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMetadata {

    AggregateId aggregateId;

    AggregateType aggregateType;

    /**
     * The version of the aggregate when the event is applied.
     */
    Version aggregateVersion;

    /**
     * The agent that caused the event (e.g. a user or system).
     */
    Agent agent;

    /**
     * The date when the event happened.
     */
    Instant date;

    boolean isSnapshot;

    public static EventMetadata of(
            AggregateId aggregateId,
            AggregateType aggregateType,
            Version aggregateVersion,
            Agent agent,
            Instant date,
            boolean isSnapshot
    ) {
        checkNotNull(aggregateId, "AggregateId must not be null");
        checkNotNull(aggregateType, "AggregateType must not be null");
        checkNotNull(aggregateVersion, "Version must not be null");
        checkNotNull(agent, "Agent must not be null");
        checkNotNull(date, "Date must not be null");

        return new EventMetadata(
                aggregateId,
                aggregateType,
                aggregateVersion,
                agent,
                date,
                isSnapshot
        );
    }

}
