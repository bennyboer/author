package de.bennyboer.author.eventsourcing.persistence;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventWithMetadata;
import de.bennyboer.author.eventsourcing.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An in-memory implementation of {@link EventSourcingRepo}.
 * This may be useful for testing purposes.
 */
public class InMemoryEventSourcingRepo implements EventSourcingRepo {

    private final Map<AggregateIdAndType, CopyOnWriteArrayList<EventWithMetadata>> eventsLookup =
            new ConcurrentHashMap<>();

    @Override
    public Mono<EventWithMetadata> insert(EventWithMetadata event) {
        AggregateIdAndType aggregateIdAndType = toAggregateIdAndType(event);

        return Mono.fromRunnable(() -> {
            var events = eventsLookup.computeIfAbsent(
                    aggregateIdAndType,
                    key -> new CopyOnWriteArrayList<>()
            );

            if (events.isEmpty()) {
                events.add(event);
                return;
            }

            var lastEvent = events.get(events.size() - 1);

            Version lastVersion = lastEvent.getMetadata().getAggregateVersion();
            Version newVersion = event.getMetadata().getAggregateVersion();

            if (!lastVersion.isPreviousTo(newVersion)) {
                throw new IllegalArgumentException(String.format(
                        "New version must be the last version + 1. Last version: %s, new version: %s",
                        lastVersion,
                        newVersion
                ));
            }

            events.add(event);
        }).thenReturn(event);
    }

    @Override
    public Mono<EventWithMetadata> findNearestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version version
    ) {
        return Mono.justOrEmpty(getEvents(AggregateIdAndType.of(aggregateId, type))
                .flatMap(events -> {
                    for (int i = Math.min(events.size() - 1, (int) version.getValue()); i >= 0; i--) {
                        var event = events.get(i);
                        if (event.getMetadata().isSnapshot()) {
                            return Optional.of(event);
                        }
                    }

                    return Optional.empty();
                }));
    }

    @Override
    public Mono<EventWithMetadata> findLatestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    ) {
        return Mono.justOrEmpty(getEvents(AggregateIdAndType.of(aggregateId, type))
                .flatMap(events -> {
                    for (int i = events.size() - 1; i >= 0; i--) {
                        var event = events.get(i);
                        if (event.getMetadata().isSnapshot()) {
                            return Optional.of(event);
                        }
                    }

                    return Optional.empty();
                }));
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion
    ) {
        return getEvents(AggregateIdAndType.of(aggregateId, type))
                .map(events -> events.subList(
                        (int) fromVersion.getValue(),
                        events.size()
                ))
                .map(Flux::fromIterable)
                .orElse(Flux.empty());
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion,
            Version version
    ) {
        return getEvents(AggregateIdAndType.of(aggregateId, type))
                .map(events -> events.subList(
                        (int) fromVersion.getValue(),
                        Math.min((int) version.getValue() + 1, events.size())
                ))
                .map(Flux::fromIterable)
                .orElse(Flux.empty());
    }

    private Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    ) {
        return Optional.ofNullable(eventsLookup.get(AggregateIdAndType.of(aggregateId, type)))
                .map(Flux::fromIterable)
                .orElse(Flux.empty());
    }

    private Optional<CopyOnWriteArrayList<EventWithMetadata>> getEvents(AggregateIdAndType aggregateIdAndType) {
        return Optional.ofNullable(eventsLookup.get(aggregateIdAndType));
    }

    private AggregateIdAndType toAggregateIdAndType(EventWithMetadata event) {
        EventMetadata metadata = event.getMetadata();
        return AggregateIdAndType.of(metadata.getAggregateId(), metadata.getAggregateType());
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class AggregateIdAndType {

        AggregateId aggregateId;

        AggregateType type;

        public static AggregateIdAndType of(AggregateId aggregateId, AggregateType type) {
            if (aggregateId == null) {
                throw new IllegalArgumentException("AggregateId must be given");
            }
            if (type == null) {
                throw new IllegalArgumentException("AggregateType must be given");
            }

            return new AggregateIdAndType(aggregateId, type);
        }

    }

}
