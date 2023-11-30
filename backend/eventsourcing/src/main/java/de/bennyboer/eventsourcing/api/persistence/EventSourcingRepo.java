package de.bennyboer.eventsourcing.api.persistence;

import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.event.EventWithMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventSourcingRepo {

    Mono<EventWithMetadata> insert(EventWithMetadata event);

    Mono<EventWithMetadata> findNearestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version version
    );

    Mono<EventWithMetadata> findLatestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    );

    Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion
    );

    Flux<EventWithMetadata> findEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion,
            Version untilVersion
    );

}
