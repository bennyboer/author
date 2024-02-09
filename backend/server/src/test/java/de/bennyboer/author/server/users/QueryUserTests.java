package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.UserDTO;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryUserTests extends UsersPluginTests {

    @Test
    void shouldGetUserDetails() {
        JavalinTest.test(getJavalin(), ((server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
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
            UserDTO user = getJsonMapper().fromJsonString(response.body().string(), UserDTO.class);
            assertThat(user.getId()).isEqualTo(userId);
            assertThat(user.getName()).isEqualTo("default");
            assertThat(user.getMail()).isEqualTo("default+test@example.com");
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
        }));
    }

    @Test
    void shouldNotBeAbleToGetUserDetailsWithoutValidToken() {
        JavalinTest.test(getJavalin(), ((server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
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

}
