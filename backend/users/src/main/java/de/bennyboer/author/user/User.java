package de.bennyboer.author.user;

import de.bennyboer.author.auth.password.EncodedPassword;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.Aggregate;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.eventsourcing.command.SnapshotCmd;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.user.anonymize.AnonymizedEvent;
import de.bennyboer.author.user.create.CreateCmd;
import de.bennyboer.author.user.create.CreatedEvent;
import de.bennyboer.author.user.image.ImageUpdatedEvent;
import de.bennyboer.author.user.image.UpdateImageCmd;
import de.bennyboer.author.user.login.LoggedInEvent;
import de.bennyboer.author.user.login.LoginCmd;
import de.bennyboer.author.user.login.LoginFailedEvent;
import de.bennyboer.author.user.login.UserLockedException;
import de.bennyboer.author.user.mail.ConfirmMailUpdateCmd;
import de.bennyboer.author.user.mail.MailUpdateConfirmedEvent;
import de.bennyboer.author.user.mail.MailUpdateRequestedEvent;
import de.bennyboer.author.user.mail.RequestMailUpdateCmd;
import de.bennyboer.author.user.password.ChangePasswordCmd;
import de.bennyboer.author.user.password.PasswordChangedEvent;
import de.bennyboer.author.user.remove.RemoveCmd;
import de.bennyboer.author.user.remove.RemovedEvent;
import de.bennyboer.author.user.rename.RenameFirstNameCmd;
import de.bennyboer.author.user.rename.RenameLastNameCmd;
import de.bennyboer.author.user.rename.RenamedFirstNameEvent;
import de.bennyboer.author.user.rename.RenamedLastNameEvent;
import de.bennyboer.author.user.snapshot.SnapshottedEvent;
import de.bennyboer.author.user.usernamechange.ChangeUserNameCmd;
import de.bennyboer.author.user.usernamechange.UserNameChangedEvent;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

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

    Mail mail;

    @Nullable
    Mail pendingMail;

    @Nullable
    MailConfirmationToken mailConfirmationToken;

    FirstName firstName;

    LastName lastName;

    Password password;

    long failedLoginAttempts;

    @Nullable
    Instant firstFailedLoginAttemptAt;

    @Nullable
    ImageId imageId;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static User init() {
        return new User(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0L,
                null,
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
            var exceptions = Set.of(SnapshotCmd.class);
            if (!exceptions.contains(cmd.getClass())) {
                throw new IllegalStateException("Cannot apply command to removed User");
            }
        }

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getName(),
                    getMail(),
                    getPendingMail().orElse(null),
                    getMailConfirmationToken().orElse(null),
                    getFirstName(),
                    getLastName(),
                    getPassword(),
                    getImageId().orElse(null),
                    getCreatedAt(),
                    getRemovedAt().orElse(null)
            ));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getName(),
                    c.getMail(),
                    c.getFirstName(),
                    c.getLastName(),
                    c.getPassword()
            ));
            case ChangeUserNameCmd c -> ApplyCommandResult.of(UserNameChangedEvent.of(c.getNewName()));
            case RenameFirstNameCmd c -> ApplyCommandResult.of(RenamedFirstNameEvent.of(c.getFirstName()));
            case RenameLastNameCmd c -> ApplyCommandResult.of(RenamedLastNameEvent.of(c.getLastName()));
            case ChangePasswordCmd c -> ApplyCommandResult.of(PasswordChangedEvent.of(c.getPassword()));
            case RequestMailUpdateCmd c -> ApplyCommandResult.of(MailUpdateRequestedEvent.of(
                    c.getMail(),
                    MailConfirmationToken.create()
            ));
            case ConfirmMailUpdateCmd c -> confirmMailUpdate(c);
            case UpdateImageCmd c -> ApplyCommandResult.of(ImageUpdatedEvent.of(
                    c.getImageId(),
                    getImageId().orElse(null)
            ));
            case RemoveCmd ignored -> ApplyCommandResult.of(RemovedEvent.of(), AnonymizedEvent.of());
            case LoginCmd c -> handleLoginCmd(c);
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public User apply(Event event, EventMetadata metadata) {
        var updatedUser = switch (event) {
            case SnapshottedEvent e -> withId(UserId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withMail(e.getMail())
                    .withPendingMail(e.getPendingMail().orElse(null))
                    .withMailConfirmationToken(e.getToken().orElse(null))
                    .withFirstName(e.getFirstName())
                    .withLastName(e.getLastName())
                    .withPassword(e.getPassword())
                    .withImageId(e.getImageId().orElse(null))
                    .withCreatedAt(e.getCreatedAt())
                    .withRemovedAt(e.getRemovedAt().orElse(null));
            case CreatedEvent e -> withId(UserId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withMail(e.getMail())
                    .withFirstName(e.getFirstName())
                    .withLastName(e.getLastName())
                    .withPassword(e.getPassword())
                    .withCreatedAt(metadata.getDate());
            case UserNameChangedEvent e -> withName(e.getNewName());
            case RenamedFirstNameEvent e -> withFirstName(e.getFirstName());
            case RenamedLastNameEvent e -> withLastName(e.getLastName());
            case RemovedEvent ignored -> withRemovedAt(metadata.getDate());
            case LoggedInEvent ignored -> withFailedLoginAttempts(0L)
                    .withFirstFailedLoginAttemptAt(null);
            case LoginFailedEvent ignored -> handleLoginFailedEvent(metadata.getDate());
            case PasswordChangedEvent e -> withPassword(e.getPassword());
            case MailUpdateRequestedEvent e -> withPendingMail(e.getMail())
                    .withMailConfirmationToken(e.getToken());
            case MailUpdateConfirmedEvent ignored -> withMail(getPendingMail().orElseThrow())
                    .withPendingMail(null)
                    .withMailConfirmationToken(null);
            case ImageUpdatedEvent e -> withImageId(e.getImageId());
            case AnonymizedEvent ignored -> withName(getName().anonymized())
                    .withMail(getMail().anonymized())
                    .withPendingMail(null)
                    .withFirstName(getFirstName().anonymized())
                    .withLastName(getLastName().anonymized());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };

        return updatedUser.withVersion(metadata.getAggregateVersion());
    }

    public Optional<ImageId> getImageId() {
        return Optional.ofNullable(imageId);
    }

    public Optional<Mail> getPendingMail() {
        return Optional.ofNullable(pendingMail);
    }

    public Optional<MailConfirmationToken> getMailConfirmationToken() {
        return Optional.ofNullable(mailConfirmationToken);
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

    private ApplyCommandResult confirmMailUpdate(ConfirmMailUpdateCmd c) {
        if (getPendingMail().isEmpty()) {
            throw new IllegalStateException("No pending mail");
        }

        if (getMailConfirmationToken().isEmpty()) {
            throw new IllegalStateException("No mail confirmation token");
        }

        Mail pendingMail = getPendingMail().orElseThrow();
        MailConfirmationToken token = getMailConfirmationToken().orElseThrow();

        if (!pendingMail.equals(c.getMail())) {
            throw new IllegalStateException("Invalid pending mail");
        }

        if (!token.equals(c.getToken())) {
            throw new IllegalStateException("Invalid mail confirmation token");
        }

        return ApplyCommandResult.of(MailUpdateConfirmedEvent.of(c.getMail()));
    }

}
