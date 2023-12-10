package de.bennyboer.author.user;

import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.eventsourcing.testing.TestEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private final UserService userService = new UserService(
            new InMemoryEventSourcingRepo(),
            new TestEventPublisher()
    );

    private final Agent systemAgent = Agent.system();

    @Test
    void shouldCreateUser() {
        // given: the name of the user to create
        var name = UserName.of("Max Mustermann");

        // when: a user is created with system agent
        var userIdAndVersion = userService.create(name, systemAgent).block();
        var userId = userIdAndVersion.getId();
        var version = userIdAndVersion.getVersion();

        // then: the user can be retrieved
        var user = userService.get(userId, version).block();
        assertEquals(name, user.getName());
    }

    @Test
    void shouldRenameUser() {
        // given: a user
        var name = UserName.of("Max Mustermann");
        var userIdAndVersion = userService.create(name, systemAgent).block();
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
        var name = UserName.of("Max Mustermann");
        var userIdAndVersion = userService.create(name, systemAgent).block();
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
        var name = UserName.of("Max Mustermann");
        var userIdAndVersion = userService.create(name, systemAgent).block();
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
        var name = UserName.of("Max Mustermann");
        var userIdAndVersion = userService.create(name, systemAgent).block();
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
        var name = UserName.of("Max Mustermann");
        var userIdAndVersion = userService.create(name, systemAgent).block();
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
        // given: the name of the user to create
        var name = UserName.of("Max Mustermann");

        // when: a user is created with a non-system agent
        var nonSystemAgent = Agent.user(UserId.create());
        Executable executable = () -> userService.create(name, nonSystemAgent).block();

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

}
