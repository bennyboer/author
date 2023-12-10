package de.bennyboer.eventsourcing;

import de.bennyboer.eventsourcing.aggregate.AggregateContainer;
import de.bennyboer.eventsourcing.patch.EventPatcher;
import de.bennyboer.eventsourcing.aggregate.Aggregate;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.command.Command;
import de.bennyboer.eventsourcing.command.SnapshotCmd;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventWithMetadata;
import de.bennyboer.eventsourcing.event.SnapshotEvent;
import de.bennyboer.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.patch.Patch;
import de.bennyboer.eventsourcing.persistence.EventSourcingRepo;
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

    EventPublisher eventPublisher;

    EventPatcher patcher;

    public EventSourcingService(
            AggregateType aggregateType,
            A initialState,
            EventSourcingRepo repo,
            EventPublisher eventPublisher,
            List<Patch> patches
    ) {
        this.aggregateType = aggregateType;
        this.initialState = initialState;
        this.repo = repo;
        this.eventPublisher = eventPublisher;
        this.patcher = EventPatcher.fromPatches(patches);
    }

    @SuppressWarnings("unchecked")
    public Mono<A> aggregateLatest(AggregateId id) {
        return aggregateLatestInContainer(id)
                .filter(AggregateContainer::hasSeenEvents)
                .map(container -> (A) container.getAggregate());
    }

    @SuppressWarnings("unchecked")
    public Mono<A> aggregate(AggregateId id, Version version) {
        return aggregateInContainer(id, version)
                .filter(AggregateContainer::hasSeenEvents)
                .map(container -> (A) container.getAggregate());
    }

    /**
     * Dispatches a command to the aggregate with the given id in its latest version.
     */
    public Mono<Version> dispatchCommandToLatest(AggregateId aggregateId, Command cmd, Agent agent) {
        return aggregateLatestInContainer(aggregateId)
                .flatMap(container -> handleCommandInAggregate(aggregateId, container, cmd, agent));
    }

    /**
     * Dispatch a command to the aggregate with the given id and version.
     * If the passed version is not the latest version of the aggregate, the command will be rejected.
     */
    public Mono<Version> dispatchCommand(AggregateId aggregateId, Version version, Command cmd, Agent agent) {
        return aggregateInContainer(aggregateId, version)
                .flatMap(container -> handleCommandInAggregate(aggregateId, container, cmd, agent));
    }

    private Mono<Version> handleCommandInAggregate(
            AggregateId aggregateId,
            AggregateContainer container,
            Command cmd,
            Agent agent
    ) {
        ApplyCommandResult result = container.getAggregate().apply(cmd);

        return saveAndPublishEvents(aggregateId, container, agent, result)
                .collectList()
                .flatMap(events -> snapshotIfNecessary(aggregateId, agent, container, events));
    }

    private Mono<Version> snapshotIfNecessary(
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

        return Mono.just(container.getVersion());
    }

    private Mono<Version> snapshot(AggregateId aggregateId, Agent agent, AggregateContainer container) {
        var snapshotCmd = SnapshotCmd.of();
        var result = container.apply(snapshotCmd);

        return saveAndPublishEvents(aggregateId, container, agent, result)
                .last()
                .map(event -> event.getMetadata().getAggregateVersion());
    }

    private Mono<AggregateContainer> aggregateLatestInContainer(AggregateId id) {
        return repo.findLatestSnapshotEventByAggregateIdAndType(id, aggregateType)
                .map(event -> event.getMetadata().getAggregateVersion())
                .defaultIfEmpty(Version.zero())
                .flatMapMany(fromVersion -> repo.findEventsByAggregateIdAndType(id, aggregateType, fromVersion))
                .reduce(AggregateContainer.init(initialState), (container, event) -> container.apply(
                        patcher.patch(event.getEvent(), event.getMetadata()),
                        event.getMetadata()
                ));
    }

    private Mono<AggregateContainer> aggregateInContainer(AggregateId id, Version version) {
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
                        patcher.patch(event.getEvent(), event.getMetadata()),
                        event.getMetadata()
                ));
    }

    private Flux<EventWithMetadata> saveAndPublishEvents(
            AggregateId aggregateId,
            AggregateContainer container,
            Agent agent,
            ApplyCommandResult result
    ) {
        Instant now = Instant.now();

        var events = result.getEvents();
        if (events.isEmpty()) {
            return Flux.empty();
        }

        var eventsWithMetadata = new ArrayList<EventWithMetadata>();
        var currentVersion = container.hasSeenEvents() ? container.getVersion().increment() : Version.zero();
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
                .concatMap(this::insertEventInRepoAndPublish);
    }

    private Mono<EventWithMetadata> insertEventInRepoAndPublish(EventWithMetadata event) {
        return repo.insert(event)
                .flatMap(e -> eventPublisher.publish(e).thenReturn(e));
    }

}
