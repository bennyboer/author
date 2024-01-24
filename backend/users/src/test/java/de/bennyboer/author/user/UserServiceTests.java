package de.bennyboer.author.user;

import de.bennyboer.author.auth.password.PasswordEncoder;
import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.testing.TestEventPublisher;
import de.bennyboer.author.testing.TestClock;
import de.bennyboer.author.user.login.UserLockedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private final TestClock clock = new TestClock();

    private final EventSourcingRepo eventSourcingRepo = new InMemoryEventSourcingRepo();

    private final UserService userService = new UserService(
            eventSourcingRepo,
            new TestEventPublisher(),
            content -> Mono.just(Token.of("TEST_TOKEN")),
            clock
    );

    private final Agent systemAgent = Agent.system();

    private final UserName defaultName = UserName.of("Max Mustermann");

    private final Mail defaultMail = Mail.of("max.mustermann+test@example.com");

    private final FirstName defaultFirstName = FirstName.of("Max");

    private final LastName defaultLastName = LastName.of("Mustermann");

    private final Password defaultPassword = Password.of("password");

    static {
        PasswordEncoder.getInstance().enableTestProfile();
    }

    @Test
    void shouldCreateUser() {
        // when: a user is created with system agent
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // then: the user can be retrieved
        var user = userService.get(userId, version).block();
        assertEquals(defaultName, user.getName());
        assertEquals(defaultMail, user.getMail());
        assertEquals(defaultFirstName, user.getFirstName());
        assertEquals(defaultLastName, user.getLastName());

        // and: the password is encoded
        assertNotEquals(defaultPassword, user.getPassword());
    }

    @Test
    void shouldNotBeAbleToCreateUserWithInvalidCharacterInUserName() {
        // when: a user is created with an @ in the user name
        Executable executable = () -> userService.create(
                UserName.of("@MaxMustermann"),
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );
        assertEquals(
                "User name must not contain '@'",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotBeAbleToCreateUserWithInvalidMail() {
        // when: a user is created with an invalid mail
        Executable executable = () -> userService.create(
                defaultName,
                Mail.of("thisisnotamail"),
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalArgumentException.class,
                executable
        );
        assertEquals(
                "Mail value 'thisisnotamail' is not valid",
                exception.getMessage()
        );
    }

    @Test
    void shouldUpdateUserName() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: the user name is changed
        var userAgent = Agent.user(userId);
        var newName = UserName.of("Maximilian Mustermann");
        userService.updateUserName(userId, version, newName, userAgent).block();

        // then: the user name has changed
        var user = userService.get(userId).block();
        assertEquals(newName, user.getName());
    }

    @Test
    void shouldUpdateImage() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: the image is updated
        var userAgent = Agent.user(userId);
        var newImageId = ImageId.of("newImageId");
        userService.updateImage(userId, version, newImageId, userAgent).block();

        // then: the image has changed
        var user = userService.get(userId).block();
        assertEquals(Optional.of(newImageId), user.getImageId());
    }

    @Test
    void shouldUpdateFirstName() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: the first name is changed
        var userAgent = Agent.user(userId);
        var newFirstName = FirstName.of("Maximilian");
        userService.renameFirstName(userId, version, newFirstName, userAgent).block();

        // then: the first name has changed
        var user = userService.get(userId).block();
        assertEquals(newFirstName, user.getFirstName());
    }

    @Test
    void shouldUpdateLastName() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: the last name is changed
        var userAgent = Agent.user(userId);
        var newLastName = LastName.of("Mustermann-Schmidt");
        userService.renameLastName(userId, version, newLastName, userAgent).block();

        // then: the last name has changed
        var user = userService.get(userId).block();
        assertEquals(newLastName, user.getLastName());
    }

    @Test
    void shouldChangePassword() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                Password.of("oldPassword"),
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: the password is changed
        var userAgent = Agent.user(userId);
        var newPassword = Password.of("newPassword");
        userService.changePassword(userId, version, newPassword, userAgent).block();

        // then: we can login with the new password
        var accessToken = userService.login(userId, newPassword).block();
        assertNotNull(accessToken);
    }

    @Test
    void shouldUpdateMail() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: the mail is updated
        var userAgent = Agent.user(userId);
        var newMail = Mail.of("new.mail+test@example.com");
        userService.updateMail(userId, version, newMail, userAgent).block();

        // then: the mail is pending
        var user = userService.get(userId).block();
        assertEquals(Optional.of(newMail), user.getPendingMail());

        // and: a confirmation token is generated
        var token = user.getMailConfirmationToken();
        assertTrue(token.isPresent());

        // when: the mail is confirmed
        userService.confirmMail(userId, newMail, token.get(), Agent.anonymous()).block();

        // then: the mail has changed
        user = userService.get(userId).block();
        assertEquals(newMail, user.getMail());
    }

    @Test
    void shouldRemoveUser() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: the user is removed
        var userAgent = Agent.user(userId);
        userService.remove(userId, version, userAgent).block();

        // then: the user is gone
        var user = userService.get(userId).block();
        assertNull(user);
    }

    @Test
    void shouldAnonymizeUserOnRemove() {
        // given: a user
        var userIdAndVersion = userService.create(
                UserName.of("MaxMuster"),
                Mail.of("max.mustermann+test@example.com"),
                FirstName.of("Max"),
                LastName.of("Mustermann"),
                Password.of("password"),
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // and: a pending mail update
        var newMail = Mail.of("max.mustermann+test@example.com");
        version = userService.updateMail(userId, version, newMail, Agent.user(userId)).block();

        // when: the user is removed
        var userAgent = Agent.user(userId);
        userService.remove(userId, version, userAgent).block();

        // then: the user is gone
        var user = userService.get(userId).block();
        assertNull(user);

        // and: the user is anonymized in the event store and all events are gone except the last one
        var events = eventSourcingRepo.findEventsByAggregateIdAndType(
                AggregateId.of(userId.getValue()),
                User.TYPE,
                Version.zero()
        ).collectList().block();
        assertEquals(1, events.size());
        var event = events.stream().findFirst().orElseThrow();
        var aggregate = User.init();
        aggregate = aggregate.apply(event.getEvent(), event.getMetadata());
        assertEquals(UserName.of("ANONYMIZED"), aggregate.getName());
        assertEquals(Mail.of("anonymized+ignored@existing.page"), aggregate.getMail());
        assertTrue(aggregate.getPendingMail().isEmpty());
        assertEquals(FirstName.of("ANONYMIZED"), aggregate.getFirstName());
        assertEquals(LastName.of("ANONYMIZED"), aggregate.getLastName());
    }

    @Test
    void shouldNotAcceptOtherCommandBeforeCreating() {
        UserId userId = UserId.create();

        // when: trying to update the user name of a non-existing user
        var userAgent = Agent.user(userId);
        Executable executable = () -> userService.updateUserName(
                userId,
                Version.zero(),
                UserName.of("Alice in Wonderland"),
                userAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "User must be initialized with CreateCmd before applying other commands",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotAcceptCommandsAfterRemoval() {
        // given: a removed user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var initialVersion = userIdAndVersion.getVersion();
        var userAgent = Agent.user(userId);
        var version = userService.remove(userId, initialVersion, userAgent).block();

        // when: trying to update the user name of the removed user
        Executable executable = () -> userService.updateUserName(
                userId,
                version,
                UserName.of("Maximilian Mustermann"),
                userAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Cannot apply command to removed User",
                exception.getMessage()
        );
    }

    @Test
    void shouldLoginGivenTheCorrectUserNameAndPassword() {
        // given: a user
        var password = Password.of("MySecretPassword");
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                password,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();

        // when: the user logs in with the correct password
        var accessToken = userService.login(userId, password).block();

        // then: an access token is returned
        assertNotNull(accessToken);
    }

    @Test
    void shouldIncreaseLoginAttemptsGivenTheWrongPassword() {
        // given: a user
        var password = Password.of("MySecretPassword");
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                password,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();

        // when: the user logs in with the wrong password
        var wrongPassword = Password.of("WrongPassword");
        var accessToken = userService.login(userId, wrongPassword).block();

        // then: no access token is returned
        assertNull(accessToken);

        // and: the failed login attempts are increased
        var user = userService.get(userId).block();
        assertEquals(1, user.getFailedLoginAttempts());

        // and: the user is not locked yet
        assertFalse(user.isLocked());
    }

    @Test
    void shouldLockUserGivenTooManyLoginAttempts() {
        // given: a user
        var password = Password.of("MySecretPassword");
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                password,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();

        // when: the user logs in with the wrong password too many times
        var wrongPassword = Password.of("WrongPassword");
        for (int i = 0; i < 10; i++) {
            userService.login(userId, wrongPassword).block();
        }

        // then: the user is locked
        var user = userService.get(userId).block();
        assertTrue(user.isLocked());

        // when: trying to login given the correct password
        Executable executable = () -> userService.login(userId, password).block();

        // then: an exception is thrown
        assertThrows(
                UserLockedException.class,
                executable
        );
    }

    @Test
    void shouldNotLockUserGivenNearlyTooManyLoginAttempts() {
        // given: a user
        var password = Password.of("MySecretPassword");
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                password,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();

        // when: the user logs in with the wrong password nearly too many times
        var wrongPassword = Password.of("WrongPassword");
        for (int i = 0; i < 9; i++) {
            userService.login(userId, wrongPassword).block();
        }

        // then: the user is not locked
        var user = userService.get(userId).block();
        assertFalse(user.isLocked());

        // when: trying to login given the correct password
        var accessToken = userService.login(userId, password).block();

        // then: an access token is returned
        assertNotNull(accessToken);
    }

    @Test
    void shouldUnlockLoginAfterSomeTime() {
        // given: a user
        var password = Password.of("MySecretPassword");
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                password,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();

        // when: the user logs in with the wrong password too many times
        var wrongPassword = Password.of("WrongPassword");
        for (int i = 0; i < 10; i++) {
            userService.login(userId, wrongPassword).block();
        }

        // then: the user is locked
        var user = userService.get(userId).block();
        assertTrue(user.isLocked());

        // when: waiting for 31 mins
        clock.add(Duration.ofMinutes(31));

        // then: the user is not locked anymore
        user = userService.get(userId).block();
        assertFalse(user.isLocked(clock.instant()));

        // when: trying to login given the correct password
        var accessToken = userService.login(userId, password).block();

        // then: an access token is returned
        assertEquals(AccessToken.of("TEST_TOKEN"), accessToken);

        // and: the user is not locked anymore
        user = userService.get(userId).block();
        assertFalse(user.isLocked());
    }

    @Test
    void shouldCreateSnapshot() {
        // given: a user
        var userIdAndVersion = userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                systemAgent
        ).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // when: updating the user name a few times
        for (int i = 0; i <= 100; i++) {
            var userAgent = Agent.user(userIdAndVersion.getId());
            version = userService.updateUserName(
                    userId,
                    version,
                    UserName.of("Max Mustermann " + i),
                    userAgent
            ).block();
        }

        // then: a snapshot is created
        var events = eventSourcingRepo.findEventsByAggregateIdAndType(
                AggregateId.of(userIdAndVersion.getId().getValue()),
                User.TYPE,
                Version.zero()
        ).collectList().block();
        var snapshot = events.stream()
                .filter(event -> event.getMetadata().isSnapshot())
                .findFirst()
                .orElseThrow();
        assertEquals(UserEvent.SNAPSHOTTED.getName(), snapshot.getEvent().getEventName());

        // when: applying another event
        var userAgent = Agent.user(userIdAndVersion.getId());
        version = userService.updateUserName(
                userId,
                version,
                UserName.of("Final Max Mustermann"),
                userAgent
        ).block();

        // then: the event is applied to the snapshot
        var finalUser = userService.get(userId, version).block();
        assertEquals(UserName.of("Final Max Mustermann"), finalUser.getName());
    }

}
