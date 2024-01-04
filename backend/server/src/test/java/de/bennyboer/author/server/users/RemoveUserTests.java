package de.bennyboer.author.server.users;

import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoveUserTests extends UsersModuleTests {

    @Test
    void shouldRemoveUser() {
        JavalinTest.test(javalin, ((server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to remove the user
            var response = client.delete(
                    "/api/users/%s?version=%d".formatted(userId, user.getVersion()),
                    null,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(response.code()).isEqualTo(204);

            // when: waiting for the permissions to be removed
            awaitPermissionRemoval(userId);

            // and: fetching the user details
            var fetchUserResponse = client.get(
                    "/api/users/%s".formatted(userId),
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 403 as the user does not have permission to fetch the user details
            assertThat(fetchUserResponse.code()).isEqualTo(403);
        }));
    }

}
