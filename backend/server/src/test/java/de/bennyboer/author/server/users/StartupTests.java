package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StartupTests extends UsersModuleTests {

    @Test
    void shouldCreateDefaultUserOnStartupWithoutPersistentUsers() {
        JavalinTest.test(javalin, (server, client) -> {
            // when: trying to login with the default user
            LoginUserResponse response = loginDefaultUser(client);

            // then: the login is successful as the default user has been created
            assertThat(response.getToken()).isNotEmpty();
            assertThat(response.getUserId()).isNotEmpty();
        });
    }

    @Test
    void shouldNotCreateDefaultUserOnStartupWhenAlreadyHavingPersistentUsers() {
        // TODO Modify TestUserLookupRepo to return a user on startup that is not the default user!

        JavalinTest.test(javalin, (server, client) -> {
            assertThat(false).isTrue(); // TODO Remove this line and implement the test!
        });
    }

}
