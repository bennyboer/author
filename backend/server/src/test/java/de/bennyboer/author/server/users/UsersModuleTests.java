package de.bennyboer.author.server.users;

import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.auth.keys.KeyPairs;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenGenerators;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.auth.token.TokenVerifiers;
import de.bennyboer.author.server.App;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.Profile;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.LoginUserRequest;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.testing.TestClock;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserName;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import io.javalin.testtools.HttpClient;

import java.io.IOException;
import java.util.List;

public abstract class UsersModuleTests {

    protected final TestUserLookupRepo userLookupRepo = createUserLookupRepo();
    protected final JsonMapper jsonMapper;
    protected final Javalin javalin;
    protected final TestClock clock = new TestClock();

    protected TestUserLookupRepo createUserLookupRepo() {
        return new TestUserLookupRepo();
    }

    {
        KeyPair keyPair = KeyPairs.read("/keys/key_pair.pem");
        TokenGenerator tokenGenerator = TokenGenerators.create(keyPair);
        TokenVerifier tokenVerifier = TokenVerifiers.create(keyPair);

        AppConfig config = AppConfig.builder()
                .profile(Profile.TESTING)
                .clock(clock)
                .tokenGenerator(tokenGenerator)
                .tokenVerifier(tokenVerifier)
                .modules(List.of(
                        (moduleConfig) -> {
                            var eventSerializer = new JsonMapperEventSerializer(
                                    moduleConfig.getJsonMapper(),
                                    UserEventTransformer::toSerialized,
                                    UserEventTransformer::fromSerialized
                            );
                            var eventSourcingRepo = RepoFactory.createEventSourcingRepo(User.TYPE, eventSerializer);

                            UsersConfig usersConfig = UsersConfig.builder()
                                    .tokenGenerator(tokenGenerator)
                                    .eventSourcingRepo(eventSourcingRepo)
                                    .permissionsRepo(RepoFactory.createPermissionsRepo("users"))
                                    .userLookupRepo(userLookupRepo)
                                    .build();

                            return new UsersModule(moduleConfig, usersConfig);
                        }
                ))
                .build();

        App app = new App(config);
        jsonMapper = app.getJsonMapper();
        javalin = app.createJavalin();
    }

    protected UserDTO getUserDetails(HttpClient client, String userId, String token) throws IOException {
        var response = client.get(
                "/api/users/%s".formatted(userId),
                (req) -> req.header("Authorization", "Bearer " + token)
        );
        return jsonMapper.fromJsonString(
                response.body().string(),
                UserDTO.class
        );
    }

    protected LoginUserResponse loginDefaultUser(HttpClient client) throws IOException {
        return loginUser(client, "default", "password");
    }

    protected LoginUserResponse loginUser(HttpClient client, String username, String password) throws IOException {
        // Wait until the asked user has been created and updated in the user lookup repo
        if (userLookupRepo.findUserIdByName(UserName.of(username)).blockOptional().isEmpty()) {
            userLookupRepo.awaitUpdate(user -> user.getName().equals(UserName.of(username)));
        }

        LoginUserRequest request = LoginUserRequest.builder()
                .name(username)
                .password(password)
                .build();
        String requestJson = jsonMapper.toJsonString(request, LoginUserRequest.class);
        var response = client.post("/api/users/login", requestJson);

        if (response.code() != 200) {
            throw new RuntimeException("Could not login user %s. Status code is %d".formatted(
                    username,
                    response.code()
            ));
        }

        return jsonMapper.fromJsonString(
                response.body().string(),
                LoginUserResponse.class
        );
    }

}
