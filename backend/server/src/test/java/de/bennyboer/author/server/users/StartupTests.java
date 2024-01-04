package de.bennyboer.author.server.users;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.server.users.persistence.lookup.LookupUser;
import de.bennyboer.author.user.UserName;
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
        // given: there is already a user in the database
        userLookupRepo.update(LookupUser.of(UserId.create(), UserName.of("TestUser"))).block();

        JavalinTest.test(javalin, (server, client) -> {
            // when: server started up
            Thread.sleep(500);

            // then: the default user has not been created
            assertThat(userLookupRepo.findUserIdByName(UserName.of("default")).block()).isNull();
        });
    }

}
