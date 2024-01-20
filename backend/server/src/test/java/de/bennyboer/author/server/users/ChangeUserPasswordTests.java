package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.requests.ChangePasswordRequest;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeUserPasswordTests extends UsersModuleTests {

    @Test
    void shouldChangeUserPassword() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to change the users password
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .password("newPassword")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, ChangePasswordRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/password?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(renameResponse.code()).isEqualTo(204);

            // and: the user can login with the new password
            var loginResponse = loginUser(client, "default", "newPassword");
            assertThat(loginResponse.getUserId()).isEqualTo(userId);
        });
    }

    @Test
    void shouldNotBeAbleToChangeUserPasswordWithoutValidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var user = getUserDetails(client, userId, loginUserResponse.getToken());

            // when: trying to change the users password with in invalid token
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .password("newPassword")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, ChangePasswordRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/password?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer invalid")
            );

            // then: the server responds with 401
            assertThat(renameResponse.code()).isEqualTo(401);

            // and: the user can still login with the old password
            var loginResponse = loginUser(client, "default", "password");
            assertThat(loginResponse.getUserId()).isEqualTo(userId);
        });
    }

}
