package de.bennyboer.author.eventsourcing.event.metadata;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
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
        checkNotNull(aggregateId, "AggregateId must be given");
        checkNotNull(aggregateType, "AggregateType must be given");
        checkNotNull(aggregateVersion, "Version must be given");
        checkNotNull(agent, "Agent must be given");
        checkNotNull(date, "Date must be given");

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
