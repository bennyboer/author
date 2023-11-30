package de.bennyboer.eventsourcing.api;

import de.bennyboer.eventsourcing.AggregateContainer;
import de.bennyboer.eventsourcing.api.aggregate.Aggregate;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.command.SnapshotCmd;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventWithMetadata;
import de.bennyboer.eventsourcing.api.event.SnapshotEvent;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import de.bennyboer.eventsourcing.api.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.api.persistence.EventSourcingRepo;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to comfortably use the event sourcing framework.
 */
@Value
@AllArgsConstructor
public class EventSourcingService<A extends Aggregate> {

    AggregateType aggregateType;

    A initialState;

    EventSourcingRepo repo;

    @SuppressWarnings("unchecked")
    public Mono<A> aggregateLatest(AggregateId id) {
        return aggregateLatestContainer(id)
                .filter(AggregateContainer::hasSeenEvents)
                .map(container -> (A) container.getAggregate());
    }

    @SuppressWarnings("unchecked")
    public Mono<A> aggregate(AggregateId id, Version version) {
        return aggregateContainer(id, version)
                .filter(AggregateContainer::hasSeenEvents)
                .map(container -> (A) container.getAggregate());
    }

    public Mono<Void> dispatchCommand(AggregateId aggregateId, Command cmd, Agent agent) {
        return aggregateLatestContainer(aggregateId)
                .flatMap(container -> {
                    ApplyCommandResult result = container.getAggregate().apply(cmd);

                    return saveEvents(aggregateId, container.getVersion(), agent, result)
                            .collectList()
                            .flatMap(events -> snapshotIfNecessary(aggregateId, agent, container, events));
                });
    }

    private Mono<Void> snapshotIfNecessary(
            AggregateId aggregateId,
            Agent agent,
            AggregateContainer container,
            List<EventWithMetadata> newEvents
    ) {
        for (EventWithMetadata event : newEvents) {
            container = container.apply(event.getEvent(), event.getMetadata());
        }

        if (container.getVersionCountFromLastSnapshot() >= container.getCountOfEventsToSnapshotAfter()) {
            return snapshot(aggregateId, agent, container);
        }

        return Mono.empty();
    }

    private Mono<Void> snapshot(AggregateId aggregateId, Agent agent, AggregateContainer container) {
        var snapshotCmd = SnapshotCmd.of();
        var result = container.apply(snapshotCmd);

        return saveEvents(aggregateId, container.getVersion(), agent, result).then();
    }

    private Mono<AggregateContainer> aggregateLatestContainer(AggregateId id) {
        return repo.findLatestSnapshotEventByAggregateIdAndType(id, aggregateType)
                .map(event -> event.getMetadata().getAggregateVersion())
                .defaultIfEmpty(Version.zero())
                .flatMapMany(fromVersion -> repo.findEventsByAggregateIdAndType(id, aggregateType, fromVersion))
                .reduce(AggregateContainer.init(initialState), (container, event) -> container.apply(
                        event.getEvent(),
                        event.getMetadata()
                ));
    }

    private Mono<AggregateContainer> aggregateContainer(AggregateId id, Version version) {
        return repo.findNearestSnapshotEventByAggregateIdAndType(id, aggregateType, version)
                .map(event -> event.getMetadata().getAggregateVersion())
                .defaultIfEmpty(Version.zero())
                .flatMapMany(fromVersion -> repo.findEventsByAggregateIdAndTypeUntilVersion(
                        id,
                        aggregateType,
                        fromVersion,
                        version
                ))
                .reduce(AggregateContainer.init(initialState), (container, event) -> container.apply(
                        event.getEvent(),
                        event.getMetadata()
                ));
    }

    private Flux<EventWithMetadata> saveEvents(
            AggregateId aggregateId,
            Version aggregateVersion,
            Agent agent,
            ApplyCommandResult result
    ) {
        Instant now = Instant.now();

        var events = result.getEvents();
        if (events.isEmpty()) {
            return Flux.empty();
        }

        var eventsWithMetadata = new ArrayList<EventWithMetadata>();
        var currentVersion = aggregateVersion.increment();
        for (Event event : events) {
            var metadata = EventMetadata.of(
                    aggregateId,
                    aggregateType,
                    currentVersion,
                    agent,
                    now,
                    event instanceof SnapshotEvent
            );

            eventsWithMetadata.add(EventWithMetadata.of(event, metadata));

            currentVersion = currentVersion.increment();
        }

        return Flux.fromIterable(eventsWithMetadata)
                .concatMap(repo::insert);
    }

}
