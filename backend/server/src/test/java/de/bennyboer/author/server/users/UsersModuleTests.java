package de.bennyboer.author.server.users;

import de.bennyboer.author.server.App;
import de.bennyboer.author.server.Profile;
import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.LoginUserRequest;
import de.bennyboer.author.server.users.api.requests.RenameUserRequest;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class UsersModuleTests {

    private final Javalin app = new App(Profile.TESTING).createJavalin();

    private final JavalinJackson jsonMapper = new JavalinJackson();

    @Test
    void shouldCreateDefaultUserOnStartupWithoutPersistentUsers() {
        JavalinTest.test(app, (server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // when: trying to login with the default user
            LoginUserRequest request = LoginUserRequest.builder()
                    .name("default")
                    .password("password")
                    .build();
            String requestJson = jsonMapper.toJsonString(request, LoginUserRequest.class);
            var response = client.post("/api/users/login", requestJson);

            // then: the server responds with 200
            assertThat(response.code()).isEqualTo(200);

            // and: the response contains an access token
            LoginUserResponse loginUserResponse = jsonMapper.fromJsonString(
                    response.body().string(),
                    LoginUserResponse.class
            );
            assertThat(loginUserResponse.getToken()).isNotEmpty();
            assertThat(loginUserResponse.getUserId()).isNotEmpty();
        });
    }

    @Test
    void shouldReturn401WhenUserNameCannotBeFound() {
        JavalinTest.test(app, (server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // when: trying to login with a user that cannot be found
            LoginUserRequest request = LoginUserRequest.builder()
                    .name("unknown")
                    .password("password")
                    .build();
            String requestJson = jsonMapper.toJsonString(request, LoginUserRequest.class);
            var response = client.post("/api/users/login", requestJson);

            // then: the server responds with 401
            assertThat(response.code()).isEqualTo(401);
        });
    }

    @Test
    void shouldReturn401WhenPasswordIsWrong() {
        JavalinTest.test(app, (server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // when: trying to login with a wrong password
            LoginUserRequest request = LoginUserRequest.builder()
                    .name("default")
                    .password("wrong")
                    .build();
            String requestJson = jsonMapper.toJsonString(request, LoginUserRequest.class);
            var response = client.post("/api/users/login", requestJson);

            // then: the server responds with 401
            assertThat(response.code()).isEqualTo(401);
        });
    }

    @Test
    void shouldRenameUser() {
        JavalinTest.test(app, (server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // given: a logged in user
            var loginUserResponse = loginDefaultUserAndReturnAccessToken(client);
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
        JavalinTest.test(app, (server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // given: a logged in user
            var loginUserResponse = loginDefaultUserAndReturnAccessToken(client);
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

    @Test
    void shouldGetUserDetails() {
        JavalinTest.test(app, ((server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // given: a logged in user
            var loginUserResponse = loginDefaultUserAndReturnAccessToken(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();

            // when: trying to get the user details
            var response = client.get(
                    "/api/users/%s".formatted(userId),
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 200
            assertThat(response.code()).isEqualTo(200);

            // and: the user details are returned
            UserDTO user = jsonMapper.fromJsonString(response.body().string(), UserDTO.class);
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getName()).isEqualTo("default");
        }));
    }

    @Test
    void shouldNotBeAbleToGetUserDetailsWithoutValidToken() {
        JavalinTest.test(app, ((server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // given: a logged in user
            var loginUserResponse = loginDefaultUserAndReturnAccessToken(client);
            var userId = loginUserResponse.getUserId();

            // when: trying to get the user details with an invalid token
            var response = client.get(
                    "/api/users/%s".formatted(userId),
                    (req) -> req.header("Authorization", "Bearer " + "invalid")
            );

            // then: the server responds with 401
            assertThat(response.code()).isEqualTo(401);
        }));
    }

    @Test
    void shouldRemoveUser() {
        JavalinTest.test(app, ((server, client) -> {
            Thread.sleep(500); // Wait until the lookup has been created

            // given: a logged in user
            var loginUserResponse = loginDefaultUserAndReturnAccessToken(client);
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

            // when: fetching the user details
            var fetchUserResponse = client.get(
                    "/api/users/%s".formatted(userId),
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 404
            assertThat(fetchUserResponse.code()).isEqualTo(404);
        }));
    }

    private UserDTO getUserDetails(HttpClient client, String userId, String token) throws IOException {
        var response = client.get(
                "/api/users/%s".formatted(userId),
                (req) -> req.header("Authorization", "Bearer " + token)
        );
        return jsonMapper.fromJsonString(
                response.body().string(),
                UserDTO.class
        );
    }

    private LoginUserResponse loginDefaultUserAndReturnAccessToken(HttpClient client) throws IOException {
        LoginUserRequest request = LoginUserRequest.builder()
                .name("default")
                .password("password")
                .build();
        String requestJson = jsonMapper.toJsonString(request, LoginUserRequest.class);
        var response = client.post("/api/users/login", requestJson);

        if (response.code() != 200) {
            throw new RuntimeException("Could not login default user. Status code is %d".formatted(response.code()));
        }

        return jsonMapper.fromJsonString(
                response.body().string(),
                LoginUserResponse.class
        );
    }

}
