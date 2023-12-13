package de.bennyboer.author.user;

import de.bennyboer.author.user.commands.CreateCmd;
import de.bennyboer.author.user.commands.LoginCmd;
import de.bennyboer.author.user.commands.RemoveCmd;
import de.bennyboer.author.user.commands.RenameCmd;
import de.bennyboer.author.user.error.UserLockedException;
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

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserService extends AggregateService<User, UserId> {

    private final Clock clock;

    public UserService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                User.TYPE,
                User.init(),
                repo,
                eventPublisher,
                List.of()
        ));

        this.clock = clock;
    }

    public UserService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        this(repo, eventPublisher, Clock.systemUTC());
    }

    public Mono<AggregateIdAndVersion<UserId>> create(UserName name, Password password, Agent agent) {
        UserId id = UserId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name, password))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> rename(UserId id, Version version, UserName name, Agent agent) {
        return dispatchCommand(id, version, agent, RenameCmd.of(name));
    }

    public Mono<Version> remove(UserId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCmd.of());
    }

    public Mono<AccessToken> login(UserId userId, Password password) {
        return dispatchCommandToLatest(userId, Agent.anonymous(), LoginCmd.of(password, clock.instant()))
                .onErrorResume(UserLockedException.class, e -> Mono.empty())
                .flatMap(version -> get(userId, version))
                .filter(User::hasNoFailedLoginAttempts)
                .flatMap(user -> generateAccessToken(user.getId()));
    }

    @Override
    protected AggregateId toAggregateId(UserId userId) {
        return AggregateId.of(userId.getValue());
    }

    @Override
    protected boolean isRemoved(User user) {
        return user.isRemoved();
    }

    private Mono<AccessToken> generateAccessToken(UserId userId) {
        // TODO Use JWT library
        return Mono.just(AccessToken.of("TEST-" + userId.getValue(), Instant.now().plus(24, ChronoUnit.HOURS)));
    }

}
