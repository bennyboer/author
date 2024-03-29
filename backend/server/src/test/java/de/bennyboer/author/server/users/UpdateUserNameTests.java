package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.UpdateUserNameRequest;
import de.bennyboer.author.user.UserName;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateUserNameTests extends UsersPluginTests {

    @Test
    void shouldUpdateUserName() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to update the user name
            UpdateUserNameRequest request = UpdateUserNameRequest.builder()
                    .name("New Name")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateUserNameRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/username?version=%d".formatted(userId, user.getVersion()),
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
    void shouldNotBeAbleToUpdateUserNameWithoutValidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var user = getUserDetails(client, userId, loginUserResponse.getToken());

            // when: trying to rename the user with an invalid token
            UpdateUserNameRequest request = UpdateUserNameRequest.builder()
                    .name("New Name")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateUserNameRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/username?version=%d".formatted(userId, user.getVersion()),
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

    @Test
    void shouldBeAbleToLoginWithTheNewName() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // and: the user name is updated
            UpdateUserNameRequest request = UpdateUserNameRequest.builder()
                    .name("New Name")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateUserNameRequest.class);
            client.post(
                    "/api/users/%s/username?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // and: awaiting the user name to be updated
            awaitUserPresenceInLookupRepo(UserName.of("New Name"));

            // when: the user is logging in with the new name
            var newLoginUserResponse = loginUser(client, "New Name", "password");

            // then: the server responds with 200
            assertThat(newLoginUserResponse).isNotNull();
        });
    }

}
