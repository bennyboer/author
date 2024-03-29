package de.bennyboer.author.server.users;

import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.auth.keys.KeyPairs;
import de.bennyboer.author.auth.token.TokenGenerator;
import de.bennyboer.author.auth.token.TokenGenerators;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.auth.token.TokenVerifiers;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.shared.PluginTest;
import de.bennyboer.author.server.shared.persistence.JsonMapperEventSerializer;
import de.bennyboer.author.server.shared.persistence.RepoFactory;
import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.api.requests.LoginUserRequest;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.server.users.permissions.UserAction;
import de.bennyboer.author.server.users.persistence.lookup.InMemoryUserLookupRepo;
import de.bennyboer.author.server.users.transformer.UserEventTransformer;
import de.bennyboer.author.testing.TestClock;
import de.bennyboer.author.user.*;
import io.javalin.testtools.HttpClient;

import java.io.IOException;
import java.util.List;

public abstract class UsersPluginTests extends PluginTest {

    protected InMemoryUserLookupRepo userLookupRepo;

    protected PermissionsRepo permissionsRepo;

    protected TestClock clock;

    private UserService userService;

    @Override
    protected AppConfig configure(AppConfig.AppConfigBuilder configBuilder) {
        userLookupRepo = new InMemoryUserLookupRepo();
        permissionsRepo = RepoFactory.createPermissionsRepo("users");
        clock = new TestClock();

        KeyPair keyPair = KeyPairs.read("/keys/key_pair.pem");
        TokenGenerator tokenGenerator = TokenGenerators.create(keyPair);
        TokenVerifier tokenVerifier = TokenVerifiers.create(keyPair);

        return configBuilder
                .clock(clock)
                .tokenGenerator(tokenGenerator)
                .tokenVerifier(tokenVerifier)
                .plugins(List.of(
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
                                    .userServiceConsumer(userService -> this.userService = userService)
                                    .build();

                            return new UsersPlugin(moduleConfig, usersConfig);
                        }
                ))
                .build();
    }

    protected String getMailConfirmationToken(String userId) {
        return userService.get(UserId.of(userId))
                .map(user -> user.getMailConfirmationToken().map(MailConfirmationToken::getValue).orElseThrow())
                .block();
    }

    protected UserDTO getUserDetails(HttpClient client, String userId, String token) throws IOException {
        var response = client.get(
                "/api/users/%s".formatted(userId),
                (req) -> req.header("Authorization", "Bearer " + token)
        );
        return getJsonMapper().fromJsonString(
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
        String requestJson = getJsonMapper().toJsonString(request, LoginUserRequest.class);
        var response = client.post("/api/users/login", requestJson);

        if (response.code() != 200) {
            throw new RuntimeException("Could not login user %s. Status code is %d".formatted(
                    username,
                    response.code()
            ));
        }

        return getJsonMapper().fromJsonString(
                response.body().string(),
                LoginUserResponse.class
        );
    }

    protected LoginUserResponse loginUserByMail(HttpClient client, String mail, String password) throws IOException {
        awaitUserSetupByMail(mail);

        LoginUserRequest request = LoginUserRequest.builder()
                .mail(mail)
                .password(password)
                .build();
        String requestJson = getJsonMapper().toJsonString(request, LoginUserRequest.class);
        var response = client.post("/api/users/login", requestJson);

        if (response.code() != 200) {
            throw new RuntimeException("Could not login user by mail %s. Status code is %d".formatted(
                    mail,
                    response.code()
            ));
        }

        return getJsonMapper().fromJsonString(
                response.body().string(),
                LoginUserResponse.class
        );
    }

    protected void awaitUserSetup(String userName) {
        UserName name = UserName.of(userName);

        awaitUserPresenceInLookupRepo(name);

        UserId userId = userLookupRepo.findUserIdByName(name).block();
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(Action.of(UserAction.READ.name()))
                .on(Resource.of(ResourceType.of(User.TYPE.getValue()), ResourceId.of(userId.getValue())));
        awaitPermissionCreation(permission, permissionsRepo);
    }

    protected void awaitUserSetupByMail(String mail) {
        Mail userMail = Mail.of(mail);

        awaitUserPresenceInLookupRepoByMail(userMail);

        UserId userId = userLookupRepo.findUserIdByMail(userMail).block();
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(Action.of(UserAction.READ.name()))
                .on(Resource.of(ResourceType.of(User.TYPE.getValue()), ResourceId.of(userId.getValue())));
        awaitPermissionCreation(permission, permissionsRepo);
    }

    protected void awaitUserCleanup(String userId) {
        UserId id = UserId.of(userId);

        awaitUserRemovalFromLookupRepo(id);

        Permission permission = Permission.builder()
                .user(id)
                .isAllowedTo(Action.of(UserAction.READ.name()))
                .on(Resource.of(ResourceType.of(User.TYPE.getValue()), ResourceId.of(id.getValue())));
        awaitPermissionRemoval(permission, permissionsRepo);
    }

    protected void awaitUserPresenceInLookupRepo(UserName name) {
        userLookupRepo.awaitUpdate(u -> u.getName().equals(name));
    }

    protected void awaitUserPresenceInLookupRepoByMail(Mail mail) {
        userLookupRepo.awaitUpdate(u -> u.getMail().equals(mail));
    }

    private void awaitUserRemovalFromLookupRepo(UserId userId) {
        userLookupRepo.awaitRemoval(userId);
    }

}
