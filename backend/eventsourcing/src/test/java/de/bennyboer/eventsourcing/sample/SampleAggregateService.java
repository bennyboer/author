package de.bennyboer.eventsourcing.sample;

import de.bennyboer.eventsourcing.EventPublisher;
import de.bennyboer.eventsourcing.EventSourcingService;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateService;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.eventsourcing.sample.commands.CreateCmd;
import de.bennyboer.eventsourcing.sample.commands.DeleteCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateDescriptionCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateTitleCmd;
import de.bennyboer.eventsourcing.sample.patches.CreatedEventPatch1;
import reactor.core.publisher.Mono;

import java.util.List;

public class SampleAggregateService extends AggregateService<SampleAggregate, String> {

    public SampleAggregateService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                SampleAggregate.TYPE,
                SampleAggregate.init(),
                repo,
                eventPublisher,
                List.of(new CreatedEventPatch1())
        ));
    }

    public Mono<Version> create(String id, String title, String description, Agent agent) {
        return dispatchCommandToLatest(id, agent, CreateCmd.of(title, description, null));
    }

    public Mono<Version> updateTitle(String id, Version version, String title, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateTitleCmd.of(title));
    }

    public Mono<Version> updateDescription(String id, Version version, String description, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateDescriptionCmd.of(description));
    }

    public Mono<Version> delete(String id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateId toAggregateId(String id) {
        return AggregateId.of(id);
    }

    @Override
    protected boolean isRemoved(SampleAggregate aggregate) {
        return aggregate.isDeleted();
    }

}
