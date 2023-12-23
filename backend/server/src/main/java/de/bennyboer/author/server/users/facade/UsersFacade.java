package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.users.api.AccessTokenDTO;
import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.server.users.transformer.AccessTokenTransformer;
import de.bennyboer.author.server.users.transformer.UserTransformer;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.user.UserService;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Value
@AllArgsConstructor
public class UsersFacade {

    private static final UserName DEFAULT_USER_NAME = UserName.of("default");

    private static final Password DEFAULT_USER_PASSWORD = Password.of("password");

    UserService userService;

    UserLookupRepo userLookupRepo;

    public Mono<UserDTO> getUser(String id) {
        UserId userId = UserId.of(id);

        return userService.get(userId)
                .map(UserTransformer::toApi);
    }

    public Mono<Void> create(String name, String password, Agent agent) {
        return userService.create(UserName.of(name), Password.of(password), agent).then();
    }

    public Mono<Void> rename(String id, long version, String name, Agent agent) {
        return userService.rename(UserId.of(id), Version.of(version), UserName.of(name), agent).then();
    }

    public Mono<Void> remove(String id, long version, Agent agent) {
        return userService.remove(UserId.of(id), Version.of(version), agent).then();
    }

    public Mono<AccessTokenDTO> login(String userName, CharSequence password) {
        return userLookupRepo.findUserIdByName(UserName.of(userName))
                .flatMap(userId -> userService.login(userId, Password.withoutValidation(password)))
                .map(AccessTokenTransformer::toApi);
    }

    public Mono<Void> updateUserLookupById(UserId userId) {
        return userService.get(userId)
                .flatMap(userLookupRepo::update)
                .then();
    }

    public Mono<Void> removeUserFromLookup(UserId userId) {
        return userLookupRepo.remove(userId);
    }

    public Mono<Void> initDefaultUserIfNecessary() {
        return userLookupRepo.countUsers()
                .filter(count -> count == 0)
                .flatMap(count -> createDefaultUser());
    }

    private Mono<Void> createDefaultUser() {
        // TODO Read config of the server to decide if default user should be created or not
        return userService
                .create(DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD, Agent.system())
                .doOnNext((idAndVersion) -> log.warn(
                        "Created default user with ID '{}', username '{}' and password '{}'",
                        idAndVersion.getId().getValue(),
                        DEFAULT_USER_NAME.getValue(),
                        DEFAULT_USER_PASSWORD.getValue()
                ))
                .then();
    }

}
