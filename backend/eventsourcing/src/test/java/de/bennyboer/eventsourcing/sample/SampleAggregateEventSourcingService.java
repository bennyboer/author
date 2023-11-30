package de.bennyboer.eventsourcing.sample;

import de.bennyboer.eventsourcing.api.EventSourcingService;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.aggregate.AggregateId;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentId;
import de.bennyboer.eventsourcing.api.event.metadata.agent.AgentType;
import de.bennyboer.eventsourcing.api.persistence.EventSourcingRepo;
import de.bennyboer.eventsourcing.sample.commands.CreateCmd;
import de.bennyboer.eventsourcing.sample.commands.DeleteCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateDescriptionCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateTitleCmd;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class SampleAggregateEventSourcingService {

    EventSourcingService<SampleAggregate> eventSourcingService;

    public SampleAggregateEventSourcingService(EventSourcingRepo repo) {
        this.eventSourcingService = new EventSourcingService<>(SampleAggregate.TYPE, SampleAggregate.init(), repo);
    }

    public Mono<SampleAggregate> get(String id) {
        return eventSourcingService.aggregateLatest(AggregateId.of(id));
    }

    public Mono<SampleAggregate> get(String id, int version) {
        return eventSourcingService.aggregate(AggregateId.of(id), Version.of(version));
    }

    public Mono<Void> create(String id, String title, String description, String userId) {
        return dispatchCommand(id, userId, CreateCmd.of(title, description));
    }

    public Mono<Void> updateTitle(String id, String title, String userId) {
        return dispatchCommand(id, userId, UpdateTitleCmd.of(title));
    }

    public Mono<Void> updateDescription(String id, String description, String userId) {
        return dispatchCommand(id, userId, UpdateDescriptionCmd.of(description));
    }

    public Mono<Void> delete(String id, String userId) {
        return dispatchCommand(id, userId, DeleteCmd.of());
    }

    private Mono<Void> dispatchCommand(String id, String userId, Command cmd) {
        AggregateId aggregateId = AggregateId.of(id);
        var agent = Agent.of(AgentType.USER, AgentId.of(userId));

        return eventSourcingService.dispatchCommand(aggregateId, cmd, agent);
    }

}
