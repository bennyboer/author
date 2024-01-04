package de.bennyboer.author.server.users;

import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.auth.keys.KeyPairs;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenGenerators;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.auth.token.TokenVerifiers;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.App;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.Profile;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessage;
import de.bennyboer.author.server.shared.messaging.permissions.AggregatePermissionEventMessageListener;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.LoginUserRequest;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.server.users.persistence.lookup.TestUserLookupRepo;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.testing.TestClock;
import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserName;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import io.javalin.testtools.HttpClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public abstract class UsersModuleTests {

    protected final TestUserLookupRepo userLookupRepo = createUserLookupRepo();
    protected final PermissionsRepo permissionsRepo = RepoFactory.createPermissionsRepo("users");
    protected final JsonMapper jsonMapper;
    protected final Javalin javalin;
    protected final TestClock clock = new TestClock();
    private final Messaging messaging;

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
                                    .permissionsRepo(permissionsRepo)
                                    .userLookupRepo(userLookupRepo)
                                    .build();

                            return new UsersModule(moduleConfig, usersConfig);
                        }
                ))
                .build();

        App app = new App(config);
        messaging = app.getMessaging();
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
        awaitUserSetup(username);

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

    protected void awaitUserSetup(String userName) {
        UserName name = UserName.of(userName);

        awaitUserPresenceInLookupRepo(name);

        UserId userId = userLookupRepo.findUserIdByName(name).block();
        awaitPermissionsSetup(userId);
    }

    protected void awaitUserCleanup(String userId) {
        UserId id = UserId.of(userId);

        awaitUserRemovalFromLookupRepo(id);
        awaitPermissionsCleanup(id);
    }

    private void awaitUserPresenceInLookupRepo(UserName name) {
        userLookupRepo.awaitUpdate(name);
    }

    private void awaitUserRemovalFromLookupRepo(UserId userId) {
        userLookupRepo.awaitRemoval(userId);
    }

    private void awaitPermissionsSetup(UserId userId) {
        CountDownLatch latch = new CountDownLatch(1);

        messaging.registerAggregatePermissionEventMessageListener(new AggregatePermissionEventMessageListener() {
            @Override
            public AggregateType aggregateType() {
                return User.TYPE;
            }

            @Override
            public Optional<UserId> userId() {
                return Optional.of(userId);
            }

            @Override
            public Mono<Void> onMessage(AggregatePermissionEventMessage message) {
                if (message.getAggregateId().equals(Optional.of(userId.getValue()))) {
                    boolean hasPermissions = !permissionsRepo.findPermissionsByUserId(userId)
                            .collectList()
                            .block()
                            .isEmpty();
                    if (hasPermissions) {
                        latch.countDown();
                    }
                }

                return Mono.empty();
            }
        });

        boolean hasPermissions = !permissionsRepo.findPermissionsByUserId(userId)
                .collectList()
                .block()
                .isEmpty();
        if (hasPermissions) {
            latch.countDown();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void awaitPermissionsCleanup(UserId userId) {
        CountDownLatch latch = new CountDownLatch(1);

        messaging.registerAggregatePermissionEventMessageListener(new AggregatePermissionEventMessageListener() {
            @Override
            public AggregateType aggregateType() {
                return User.TYPE;
            }

            @Override
            public Optional<UserId> userId() {
                return Optional.of(userId);
            }

            @Override
            public Mono<Void> onMessage(AggregatePermissionEventMessage message) {
                if (message.getAggregateId().equals(Optional.of(userId.getValue()))) {
                    boolean hasPermissions = !permissionsRepo.findPermissionsByUserId(userId)
                            .collectList()
                            .block()
                            .isEmpty();
                    if (!hasPermissions) {
                        latch.countDown();
                    }
                }

                return Mono.empty();
            }
        });

        boolean hasPermissions = !permissionsRepo.findPermissionsByUserId(userId)
                .collectList()
                .block()
                .isEmpty();
        if (!hasPermissions) {
            latch.countDown();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
