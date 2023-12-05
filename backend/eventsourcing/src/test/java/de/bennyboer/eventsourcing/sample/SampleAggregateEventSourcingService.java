package de.bennyboer.eventsourcing.sample;

import de.bennyboer.eventsourcing.api.EventPublisher;
import de.bennyboer.eventsourcing.api.EventSourcingService;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentId;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentType;
import de.bennyboer.eventsourcing.api.patch.Patch;
import de.bennyboer.eventsourcing.api.persistence.EventSourcingRepo;
import de.bennyboer.eventsourcing.sample.commands.CreateCmd;
import de.bennyboer.eventsourcing.sample.commands.DeleteCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateDescriptionCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateTitleCmd;
import de.bennyboer.eventsourcing.sample.patches.CreatedEventPatch1;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.util.List;

@Value
public class SampleAggregateEventSourcingService {

    EventSourcingService<SampleAggregate> eventSourcingService;

    public SampleAggregateEventSourcingService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        List<Patch> patches = List.of(new CreatedEventPatch1());

        this.eventSourcingService = new EventSourcingService<>(
                SampleAggregate.TYPE,
                SampleAggregate.init(),
                repo,
                eventPublisher,
                patches
        );
    }

    public Mono<SampleAggregate> get(String id) {
        return eventSourcingService.aggregateLatest(AggregateId.of(id));
    }

    public Mono<SampleAggregate> get(String id, int version) {
        return eventSourcingService.aggregate(AggregateId.of(id), Version.of(version));
    }

    public Mono<Long> create(String id, String title, String description, String userId) {
        return dispatchCommandToLatest(id, userId, CreateCmd.of(title, description, null));
    }

    public Mono<Long> updateTitle(String id, long version, String title, String userId) {
        return dispatchCommand(id, version, userId, UpdateTitleCmd.of(title));
    }

    public Mono<Long> updateDescription(String id, long version, String description, String userId) {
        return dispatchCommand(id, version, userId, UpdateDescriptionCmd.of(description));
    }

    public Mono<Long> delete(String id, long version, String userId) {
        return dispatchCommand(id, version, userId, DeleteCmd.of());
    }

    private Mono<Long> dispatchCommand(String id, long version, String userId, Command cmd) {
        AggregateId aggregateId = AggregateId.of(id);
        var agent = Agent.of(AgentType.USER, AgentId.of(userId));

        return eventSourcingService.dispatchCommand(aggregateId, Version.of(version), cmd, agent)
                .map(Version::getValue);
    }

    private Mono<Long> dispatchCommandToLatest(String id, String userId, Command cmd) {
        AggregateId aggregateId = AggregateId.of(id);
        var agent = Agent.of(AgentType.USER, AgentId.of(userId));

        return eventSourcingService.dispatchCommandToLatest(aggregateId, cmd, agent)
                .map(Version::getValue);
    }

}
