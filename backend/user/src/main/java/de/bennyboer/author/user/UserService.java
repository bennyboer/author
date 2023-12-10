package de.bennyboer.author.user;

import de.bennyboer.author.user.commands.CreateCmd;
import de.bennyboer.author.user.commands.RemoveCmd;
import de.bennyboer.author.user.commands.RenameCmd;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.EventPublisher;
import de.bennyboer.eventsourcing.EventSourcingService;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.eventsourcing.aggregate.AggregateService;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.persistence.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.util.List;

public class UserService extends AggregateService<User, UserId> {

    public UserService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                User.TYPE,
                User.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<UserId>> create(UserName name, Agent agent) {
        UserId id = UserId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(UserId id, Version version, UserName name, Agent agent) {
        return dispatchCommand(id, version, agent, RenameCmd.of(name));
    }

    public Mono<Version> remove(UserId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCmd.of());
    }

    @Override
    protected AggregateId toAggregateId(UserId userId) {
        return AggregateId.of(userId.getValue());
    }

    @Override
    protected boolean isRemoved(User user) {
        return user.isRemoved();
    }

}
