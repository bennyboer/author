package de.bennyboer.eventsourcing.sample;

import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.EventPublisher;
import de.bennyboer.eventsourcing.EventSourcingService;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateService;
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

    public Mono<Version> create(String id, String title, String description, String userId) {
        return dispatchCommandToLatest(id, UserId.of(userId), CreateCmd.of(title, description, null));
    }

    public Mono<Version> updateTitle(String id, Version version, String title, String userId) {
        return dispatchCommand(id, version, UserId.of(userId), UpdateTitleCmd.of(title));
    }

    public Mono<Version> updateDescription(String id, Version version, String description, String userId) {
        return dispatchCommand(id, version, UserId.of(userId), UpdateDescriptionCmd.of(description));
    }

    public Mono<Version> delete(String id, Version version, String userId) {
        return dispatchCommand(id, version, UserId.of(userId), DeleteCmd.of());
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
