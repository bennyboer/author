package de.bennyboer.author.user;

import de.bennyboer.author.auth.EncodedPassword;
import de.bennyboer.author.user.commands.CreateCmd;
import de.bennyboer.author.user.commands.LoginCmd;
import de.bennyboer.author.user.commands.RemoveCmd;
import de.bennyboer.author.user.commands.RenameCmd;
import de.bennyboer.author.user.error.UserLockedException;
import de.bennyboer.author.user.events.*;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.Aggregate;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.command.Command;
import de.bennyboer.eventsourcing.command.SnapshotCmd;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("USER");

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 10;

    private static final Duration LOCK_DURATION_DUE_TO_FAILED_LOGIN_ATTEMPTS = Duration.ofMinutes(30);

    UserId id;

    Version version;

    UserName name;

    Password password;

    long failedLoginAttempts;

    @Nullable
    Instant firstFailedLoginAttemptAt;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static User init() {
        return new User(
                null,
                null,
                null,
                null,
                0L,
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        var isInitialized = Optional.ofNullable(id).isPresent();
        var isCreateCmd = cmd instanceof CreateCmd;
        if (!isInitialized && !isCreateCmd) {
            throw new IllegalStateException(
                    "User must be initialized with CreateCmd before applying other commands"
            );
        }

        if (isRemoved()) {
            throw new IllegalStateException("Cannot apply command to removed User");
        }

        if (!isAllowedAgent(agent, cmd)) {
            throw new IllegalStateException("Agent is not allowed to apply command");
        }

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(this));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(c));
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c));
            case RemoveCmd ignored -> ApplyCommandResult.of(RemovedEvent.of());
            case LoginCmd c -> handleLoginCmd(c);
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public User apply(Event event, EventMetadata metadata) {
        var updatedUser = switch (event) {
            case SnapshottedEvent e -> withId(UserId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withPassword(e.getPassword())
                    .withCreatedAt(e.getCreatedAt());
            case CreatedEvent e -> withId(UserId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withPassword(e.getPassword())
                    .withCreatedAt(metadata.getDate());
            case RenamedEvent e -> withName(e.getNewName());
            case RemovedEvent ignored -> withRemovedAt(metadata.getDate());
            case LoggedInEvent ignored -> withFailedLoginAttempts(0L)
                    .withFirstFailedLoginAttemptAt(null);
            case LoginFailedEvent ignored -> handleLoginFailedEvent(metadata.getDate());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };

        return updatedUser.withVersion(metadata.getAggregateVersion());
    }

    public boolean isRemoved() {
        return getRemovedAt().isPresent();
    }

    public boolean isLocked() {
        return isLocked(Instant.now());
    }

    public boolean isLocked(Instant now) {
        return isLockedDueToTooManyFailedLoginAttempts(now);
    }

    public boolean hasFailedLoginAttempts() {
        return getFailedLoginAttempts() > 0;
    }

    public boolean hasNoFailedLoginAttempts() {
        return !hasFailedLoginAttempts();
    }

    private boolean isLockedDueToTooManyFailedLoginAttempts(Instant now) {
        return getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS
                && !isUnlockedAgainAsSomeTimeHasPassedSinceFirstFailedLoginAttempt(now);
    }

    private boolean isUnlockedAgainAsSomeTimeHasPassedSinceFirstFailedLoginAttempt(Instant now) {
        return getFirstFailedLoginAttemptAt()
                .map(firstFailedLoginAttemptAt -> firstFailedLoginAttemptAt
                        .plus(LOCK_DURATION_DUE_TO_FAILED_LOGIN_ATTEMPTS)
                        .isBefore(now))
                .orElse(false);
    }

    private Optional<Instant> getFirstFailedLoginAttemptAt() {
        return Optional.ofNullable(firstFailedLoginAttemptAt);
    }

    private Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

    private boolean isAllowedAgent(Agent agent, Command cmd) {
        if (agent.isSystem()) {
            return true;
        }

        if (agent.isAnonymous() && cmd instanceof LoginCmd) {
            return true;
        }

        return agent.getUserId()
                .map(id -> id.equals(this.id))
                .orElse(false);
    }

    private ApplyCommandResult handleLoginCmd(LoginCmd cmd) {
        if (isLocked(cmd.getNow())) {
            throw new UserLockedException(String.format(
                    "User is locked due to too many failed login attempts. Next login attempt is allowed at %s",
                    getFirstFailedLoginAttemptAt()
                            .map(firstFailedLoginAttemptAt -> firstFailedLoginAttemptAt
                                    .plus(LOCK_DURATION_DUE_TO_FAILED_LOGIN_ATTEMPTS))
                            .orElse(Instant.now())
            ));
        }

        Password pwd = cmd.getPassword();

        EncodedPassword storedEncodedPassword = EncodedPassword.ofEncoded(password.getValue());
        boolean isPasswordCorrect = storedEncodedPassword.matches(pwd.getValue());
        if (!isPasswordCorrect) {
            return ApplyCommandResult.of(LoginFailedEvent.of());
        }

        return ApplyCommandResult.of(LoggedInEvent.of());
    }

    private User handleLoginFailedEvent(Instant date) {
        boolean isUnlockedAgain = isUnlockedAgainAsSomeTimeHasPassedSinceFirstFailedLoginAttempt(date);
        if (isUnlockedAgain) {
            return withFailedLoginAttempts(1L)
                    .withFirstFailedLoginAttemptAt(date);
        }

        var result = withFailedLoginAttempts(getFailedLoginAttempts() + 1L);

        if (getFirstFailedLoginAttemptAt().isEmpty()) {
            result = result.withFirstFailedLoginAttemptAt(date);
        }

        return result;
    }

}
