package de.bennyboer.author.server.users;

import de.bennyboer.author.server.users.api.requests.LoginUserRequest;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.user.UserName;
import io.javalin.testtools.JavalinTest;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

public class LoginTests extends UsersModuleTests {

    @Test
    void shouldReturn401WhenUserNameCannotBeFound() {
        JavalinTest.test(javalin, (server, client) -> {
            // given: the user lookup has been updated after startup
            userLookupRepo.awaitUpdate(user -> user.getName().equals(UserName.of("default")));

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
        JavalinTest.test(javalin, (server, client) -> {
            // given: the user lookup has been updated after startup
            userLookupRepo.awaitUpdate(user -> user.getName().equals(UserName.of("default")));

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
    void shouldLockTheUserAfter10UnsuccessfulLoginAttempts() {
        JavalinTest.test(javalin, (server, client) -> {
            // when: trying to login with a wrong password 10 times
            for (int i = 0; i < 10; i++) {
                try {
                    loginUser(client, "default", "wrong");
                } catch (Exception e) {
                    // ignore
                }
            }

            // and: trying to login with the correct password
            LoginUserRequest request = LoginUserRequest.builder()
                    .name("default")
                    .password("password")
                    .build();
            String requestJson = jsonMapper.toJsonString(request, LoginUserRequest.class);
            var response = client.post("/api/users/login", requestJson);

            // then: the server responds with 429
            assertThat(response.code()).isEqualTo(429);
        });
    }

    @Test
    void shouldNotLockTheUserAfter10UnsuccessfulLoginAttemptsWhenOneSuccessfulHappenedInBetween() {
        JavalinTest.test(javalin, (server, client) -> {
            // when: trying to login with a wrong password 9 times
            for (int i = 0; i < 9; i++) {
                try {
                    loginUser(client, "default", "wrong");
                } catch (Exception e) {
                    // ignore
                }
            }

            // and: trying to login with the correct password
            LoginUserResponse response = loginUser(client, "default", "password");

            // then: the login succeeds
            assertThat(response.getToken()).isNotNull();

            // when: trying to login with a wrong password 5 times
            for (int i = 0; i < 5; i++) {
                try {
                    loginUser(client, "default", "wrong");
                } catch (Exception e) {
                    // ignore
                }
            }

            // and: trying to login with the correct password
            response = loginUser(client, "default", "password");

            // then: the login succeeds
            assertThat(response.getToken()).isNotNull();
        });
    }

    @Test
    void shouldUnlockUserAfter30Minutes() {
        JavalinTest.test(javalin, (server, client) -> {
            // when: trying to login with a wrong password 10 times
            for (int i = 0; i < 10; i++) {
                try {
                    loginUser(client, "default", "wrong");
                } catch (Exception e) {
                    // ignore
                }
            }

            // and: trying to login with the correct password
            ThrowableAssert.ThrowingCallable callable = () -> loginUser(client, "default", "password");

            // then: the server responds with 429
            assertThatException()
                    .isThrownBy(callable)
                    .withMessage("Could not login user default. Status code is 429");

            // when: waiting 30 minutes
            clock.add(Duration.ofMinutes(30));

            // and: trying to login with the correct password
            LoginUserResponse response = loginUser(client, "default", "password");

            // then: the login succeeds
            assertThat(response.getToken()).isNotNull();
        });
    }

}
