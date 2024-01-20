package de.bennyboer.author.user;

import de.bennyboer.author.auth.token.TokenContent;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.EventSourcingService;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.author.eventsourcing.aggregate.AggregateService;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.user.create.CreateCmd;
import de.bennyboer.author.user.login.LoginCmd;
import de.bennyboer.author.user.mail.ConfirmMailUpdateCmd;
import de.bennyboer.author.user.mail.RequestMailUpdateCmd;
import de.bennyboer.author.user.password.ChangePasswordCmd;
import de.bennyboer.author.user.remove.RemoveCmd;
import de.bennyboer.author.user.rename.RenameFirstNameCmd;
import de.bennyboer.author.user.rename.RenameLastNameCmd;
import de.bennyboer.author.user.usernamechange.ChangeUserNameCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class UserService extends AggregateService<User, UserId> {

    private final TokenGenerator tokenGenerator;

    private final Clock clock;

    public UserService(
            EventSourcingRepo repo,
            EventPublisher eventPublisher,
            TokenGenerator tokenGenerator,
            Clock clock
    ) {
        super(new EventSourcingService<>(
                User.TYPE,
                User.init(),
                repo,
                eventPublisher,
                List.of()
        ));

        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
    }

    public Mono<AggregateIdAndVersion<UserId>> create(
            UserName name,
            Mail mail,
            FirstName firstName,
            LastName lastName,
            Password password,
            Agent agent
    ) {
        UserId id = UserId.create();
        CreateCmd cmd = CreateCmd.of(name, mail, firstName, lastName, password);

        return dispatchCommandToLatest(id, agent, cmd)
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> updateUserName(UserId id, Version version, UserName name, Agent agent) {
        return dispatchCommand(id, version, agent, ChangeUserNameCmd.of(name));
    }

    public Mono<Version> renameFirstName(UserId id, Version version, FirstName firstName, Agent agent) {
        return dispatchCommand(id, version, agent, RenameFirstNameCmd.of(firstName));
    }

    public Mono<Version> renameLastName(UserId id, Version version, LastName lastName, Agent agent) {
        return dispatchCommand(id, version, agent, RenameLastNameCmd.of(lastName));
    }

    public Mono<Version> updateMail(UserId id, Version version, Mail mail, Agent agent) {
        return dispatchCommand(id, version, agent, RequestMailUpdateCmd.of(mail));
    }

    public Mono<Version> confirmMail(UserId userId, Mail mail, MailConfirmationToken token, Agent agent) {
        return dispatchCommandToLatest(userId, agent, ConfirmMailUpdateCmd.of(mail, token));
    }

    public Mono<Version> changePassword(UserId id, Version version, Password password, Agent agent) {
        return dispatchCommand(id, version, agent, ChangePasswordCmd.of(password));
    }

    public Mono<Version> remove(UserId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCmd.of());
    }

    public Mono<AccessToken> login(UserId userId, Password password) {
        return dispatchCommandToLatest(userId, Agent.anonymous(), LoginCmd.of(password, clock.instant()))
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
        TokenContent content = TokenContent.user(userId);

        return tokenGenerator
                .generate(content)
                .map(token -> AccessToken.of(token.getValue()));
    }

}
