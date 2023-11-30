package de.bennyboer.eventsourcing.api.event.metadata;

import de.bennyboer.eventsourcing.api.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

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
        if (aggregateId == null) {
            throw new IllegalArgumentException("AggregateId must not be null");
        }
        if (aggregateType == null) {
            throw new IllegalArgumentException("AggregateType must not be null");
        }
        if (aggregateVersion == null) {
            throw new IllegalArgumentException("Version must not be null");
        }
        if (agent == null) {
            throw new IllegalArgumentException("Agent must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }

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
