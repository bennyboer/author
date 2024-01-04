package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.RenameUserRequest;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RenameUserTests extends UsersModuleTests {

    @Test
    void shouldRenameUser() {
        JavalinTest.test(javalin, (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to rename the user
            RenameUserRequest request = RenameUserRequest.builder()
                    .name("New Name")
                    .build();
            String requestJson = jsonMapper.toJsonString(request, RenameUserRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/rename?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(renameResponse.code()).isEqualTo(204);

            // and: the user can be found with the new name
            UserDTO updatedUser = getUserDetails(client, userId, token);
            assertThat(updatedUser.getName()).isEqualTo("New Name");
        });
    }

    @Test
    void shouldNotBeAbleToRenameUserWithoutValidToken() {
        JavalinTest.test(javalin, (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var user = getUserDetails(client, userId, loginUserResponse.getToken());

            // when: trying to rename the user with an invalid token
            RenameUserRequest request = RenameUserRequest.builder()
                    .name("New Name")
                    .build();
            String requestJson = jsonMapper.toJsonString(request, RenameUserRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/rename?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + "invalid")
            );

            // then: the server responds with 401
            assertThat(renameResponse.code()).isEqualTo(401);

            // and: the user cannot be found with the new name
            UserDTO updatedUser = getUserDetails(client, userId, loginUserResponse.getToken());
            assertThat(updatedUser.getName()).isEqualTo("default");
        });
    }

}
