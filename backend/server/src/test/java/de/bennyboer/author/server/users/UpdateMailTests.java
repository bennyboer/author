package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.ConfirmMailRequest;
import de.bennyboer.author.server.users.api.requests.UpdateMailRequest;
import de.bennyboer.author.user.Mail;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateMailTests extends UsersPluginTests {

    @Test
    void shouldUpdateUserMail() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to update the users mail
            UpdateMailRequest request = UpdateMailRequest.builder()
                    .mail("new.mail+test@example.com")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateMailRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/mail?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(renameResponse.code()).isEqualTo(204);

            // and: the user cannot be found with the new mail yet
            UserDTO updatedUser = getUserDetails(client, userId, loginUserResponse.getToken());
            assertThat(updatedUser.getMail()).isNotEqualTo("new.mail+test@example.com");

            // when: the mail is confirmed
            String mailConfirmationToken = getMailConfirmationToken(userId);
            ConfirmMailRequest confirmMailRequest = ConfirmMailRequest.builder()
                    .mail("new.mail+test@example.com")
                    .token(mailConfirmationToken)
                    .build();
            String confirmMailRequestJson = getJsonMapper().toJsonString(confirmMailRequest, ConfirmMailRequest.class);
            var confirmMailResponse = client.post(
                    "/api/users/%s/mail/confirm".formatted(userId),
                    confirmMailRequestJson
            );

            // then: the server responds with 204
            assertThat(confirmMailResponse.code()).isEqualTo(204);

            // and: the user can be found with the new mail
            updatedUser = getUserDetails(client, userId, token);
            assertThat(updatedUser.getMail()).isEqualTo("new.mail+test@example.com");

            // when: waiting for the user to be updated in the lookup repo
            awaitUserPresenceInLookupRepoByMail(Mail.of("new.mail+test@example.com"));

            // and: logging in with the new mail
            var newLoginUserResponse = loginUserByMail(client, "new.mail+test@example.com", "password");

            // then: the server responds with 200
            assertThat(newLoginUserResponse).isNotNull();
        });
    }

    @Test
    void shouldNotBeAbleToUpdateUserMailWithoutValidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var user = getUserDetails(client, userId, loginUserResponse.getToken());

            // when: trying to rename the user with an invalid token
            UpdateMailRequest request = UpdateMailRequest.builder()
                    .mail("new.mail+test@example.com")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateMailRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/mail?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + "invalid")
            );

            // then: the server responds with 401
            assertThat(renameResponse.code()).isEqualTo(401);

            // and: the user cannot be found with the new mail but instead with the old one
            UserDTO updatedUser = getUserDetails(client, userId, loginUserResponse.getToken());
            assertThat(updatedUser.getMail()).isEqualTo("default+test@example.com");
        });
    }

    @Test
    void shouldNotBeAbleToConfirmMailWithoutValidToken() {
        JavalinTest.test(getJavalin(), (server, client) -> {
            // given: a logged in user
            var loginUserResponse = loginDefaultUser(client);
            var userId = loginUserResponse.getUserId();
            var token = loginUserResponse.getToken();
            var user = getUserDetails(client, userId, token);

            // when: trying to update the users mail
            UpdateMailRequest request = UpdateMailRequest.builder()
                    .mail("new.mail+test@example.com")
                    .build();
            String requestJson = getJsonMapper().toJsonString(request, UpdateMailRequest.class);
            var renameResponse = client.post(
                    "/api/users/%s/mail?version=%d".formatted(userId, user.getVersion()),
                    requestJson,
                    (req) -> req.header("Authorization", "Bearer " + token)
            );

            // then: the server responds with 204
            assertThat(renameResponse.code()).isEqualTo(204);

            // when: the mail is confirmed with an invalid token
            ConfirmMailRequest confirmMailRequest = ConfirmMailRequest.builder()
                    .mail("new.mail+test@example.com")
                    .token("invalid")
                    .build();
            String confirmMailRequestJson = getJsonMapper().toJsonString(confirmMailRequest, ConfirmMailRequest.class);
            var confirmMailResponse = client.post(
                    "/api/users/%s/mail/confirm".formatted(userId),
                    confirmMailRequestJson
            );

            // then: the server responds with 401
            assertThat(confirmMailResponse.code()).isEqualTo(401);

            // when: the mail is confirmed with an invalid mail but correct token
            confirmMailRequest = ConfirmMailRequest.builder()
                    .mail("invalid+test@example.com")
                    .token(getMailConfirmationToken(userId))
                    .build();
            confirmMailRequestJson = getJsonMapper().toJsonString(confirmMailRequest, ConfirmMailRequest.class);
            confirmMailResponse = client.post(
                    "/api/users/%s/mail/confirm".formatted(userId),
                    confirmMailRequestJson
            );

            // then: the server responds with 401
            assertThat(confirmMailResponse.code()).isEqualTo(401);
        });
    }

}
