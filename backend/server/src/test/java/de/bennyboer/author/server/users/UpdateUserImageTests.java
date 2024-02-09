package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.UpdateImageRequest;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateUserImageTests extends UsersPluginTests {

    @Test
    void shouldUpdateImage() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to update the users image
            UpdateImageRequest request = UpdateImageRequest.builder()
                    .imageId("abcdefgh")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateImageRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/image?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(renameResponse.code()).isEqualTo(204);

            // and: the user can be found with the new image
            UserDTO updatedUser = getUserDetails(client, userId, token);
            assertThat(updatedUser.getImageId()).isEqualTo(Optional.of("abcdefgh"));
        });
    }

    @Test
    void shouldNotBeAbleToUpdateImageGivenAnInvalidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var user = getUserDetails(client, userId, loginUserResponse.getToken());

            // when: trying to update the image with an invalid token
            UpdateImageRequest request = UpdateImageRequest.builder()
                    .imageId("abcdefgh")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateImageRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/image?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + "invalid")
            );

            // then: the server responds with 401
            assertThat(renameResponse.code()).isEqualTo(401);

            // and: the user cannot be found with the new image
            UserDTO updatedUser = getUserDetails(client, userId, loginUserResponse.getToken());
            assertThat(updatedUser.getImageId()).isEqualTo(Optional.empty());
        });
    }

}
