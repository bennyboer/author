package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.RenameFirstNameRequest;
import de.bennyboer.author.server.users.api.requests.RenameLastNameRequest;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RenameUserTests extends UsersModuleTests {

    @Test
    void shouldRenameFirstName() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to update the users first name
            RenameFirstNameRequest request = RenameFirstNameRequest.builder()
                    .firstName("New First Name")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, RenameFirstNameRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/rename/firstname?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(renameResponse.code()).isEqualTo(204);

            // and: the user can be found with the new first name
            UserDTO updatedUser = getUserDetails(client, userId, token);
            assertThat(updatedUser.getFirstName()).isEqualTo("New First Name");
        });
    }

    @Test
    void shouldRenameLastName() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to update the users last name
            RenameLastNameRequest request = RenameLastNameRequest.builder()
                    .lastName("New Last Name")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, RenameLastNameRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/rename/lastname?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(renameResponse.code()).isEqualTo(204);

            // and: the user can be found with the new last name
            UserDTO updatedUser = getUserDetails(client, userId, token);
            assertThat(updatedUser.getLastName()).isEqualTo("New Last Name");
        });
    }

    @Test
    void shouldNotBeAbleToRenameFirstNameWithoutValidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var user = getUserDetails(client, userId, loginUserResponse.getToken());

            // when: trying to rename the first name with an invalid token
            RenameFirstNameRequest request = RenameFirstNameRequest.builder()
                    .firstName("New First Name")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, RenameFirstNameRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/rename/firstname?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + "invalid")
            );

            // then: the server responds with 401
            assertThat(renameResponse.code()).isEqualTo(401);

            // and: the user cannot be found with the new first name
            UserDTO updatedUser = getUserDetails(client, userId, loginUserResponse.getToken());
            assertThat(updatedUser.getFirstName()).isEqualTo("John");
        });
    }

    @Test
    void shouldNotBeAbleToRenameLastNameWithoutValidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var user = getUserDetails(client, userId, loginUserResponse.getToken());

            // when: trying to rename the first name with an invalid token
            RenameLastNameRequest request = RenameLastNameRequest.builder()
                    .lastName("New Last Name")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, RenameLastNameRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/rename/lastname?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + "invalid")
            );

            // then: the server responds with 401
            assertThat(renameResponse.code()).isEqualTo(401);

            // and: the user cannot be found with the new last name
            UserDTO updatedUser = getUserDetails(client, userId, loginUserResponse.getToken());
            assertThat(updatedUser.getFirstName()).isEqualTo("Doe");
        });
    }

}
