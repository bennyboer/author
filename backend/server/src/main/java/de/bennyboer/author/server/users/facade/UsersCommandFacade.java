package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.server.users.permissions.UserPermissionsService;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.user.Password;
import de.bennyboer.author.user.UserName;
import de.bennyboer.author.user.UserService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.users.permissions.UserAction.*;

@Value
@AllArgsConstructor
public class UsersCommandFacade {

    UserService userService;

    UserPermissionsService permissionsService;

    UserLookupRepo userLookupRepo;

    public Mono<Void> create(String name, String password, Agent agent) {
        return permissionsService.assertHasPermission(agent, CREATE, UserId.create())
                .then(userService.create(UserName.of(name), Password.of(password), agent))
                .then();
    }

    public Mono<Void> rename(String id, long version, String name, Agent agent) {
        UserId userId = UserId.of(id);

        return permissionsService.assertHasPermission(agent, RENAME, userId)
                .then(userService.rename(userId, Version.of(version), UserName.of(name), agent))
                .then();
    }

    public Mono<Void> remove(String id, long version, Agent agent) {
        UserId userId = UserId.of(id);

        return permissionsService.assertHasPermission(agent, REMOVE, userId)
                .then(userService.remove(userId, Version.of(version), agent))
                .then();
    }

    public Mono<LoginUserResponse> login(String userName, CharSequence password) {
        return userLookupRepo.findUserIdByName(UserName.of(userName))
                .flatMap(userId -> userService.login(userId, Password.withoutValidation(password))
                        .map(token -> LoginUserResponse.builder()
                                .token(token.getValue())
                                .userId(userId.getValue())
                                .build()
                        ));
    }

}
