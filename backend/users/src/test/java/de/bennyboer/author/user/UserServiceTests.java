package de.bennyboer.author.user;

import de.bennyboer.author.auth.password.PasswordEncoder;
import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.author.eventsourcing.testing.TestEventPublisher;
import de.bennyboer.author.testing.TestClock;
import de.bennyboer.author.user.login.UserLockedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private final TestClock clock = new TestClock();

    private final UserService userService = new UserService(
            new InMemoryEventSourcingRepo(),
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
    void shouldRenameUser() {
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

        // when: the user is renamed
        var userAgent = Agent.user(userId);
        var newName = UserName.of("Maximilian Mustermann");
        userService.rename(userId, version, newName, userAgent).block();

        // then: the user name has changed
        var user = userService.get(userId).block();
        assertEquals(newName, user.getName());
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
    void shouldNotAcceptOtherCommandBeforeCreating() {
        UserId userId = UserId.create();

        // when: trying to rename a non-existing user
        var userAgent = Agent.user(userId);
        Executable executable = () -> userService.rename(
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

        // when: trying to rename the removed user
        Executable executable = () -> userService.rename(
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
    void shouldNotAllowRenamingAsAnotherUser() {
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

        // when: trying to rename the user as another user
        var userAgent = Agent.user(UserId.create());
        Executable executable = () -> userService.rename(
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
                "Agent is not allowed to apply command",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotAllowRemovingAsAnotherUser() {
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

        // when: trying to remove the user as another user
        var userAgent = Agent.user(UserId.create());
        Executable executable = () -> userService.remove(
                userId,
                version,
                userAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Agent is not allowed to apply command",
                exception.getMessage()
        );
    }

    @Test
    void shouldNotCreateUserWhenNotSystemAgent() {
        // when: a user is created with a non-system agent
        var nonSystemAgent = Agent.user(UserId.create());
        Executable executable = () -> userService.create(
                defaultName,
                defaultMail,
                defaultFirstName,
                defaultLastName,
                defaultPassword,
                nonSystemAgent
        ).block();

        // then: an exception is thrown
        var exception = assertThrows(
                IllegalStateException.class,
                executable
        );
        assertEquals(
                "Agent is not allowed to apply command",
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

}
