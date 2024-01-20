package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.server.users.permissions.UserPermissionsService;
import de.bennyboer.author.server.users.persistence.lookup.UserLookupRepo;
import de.bennyboer.author.user.*;
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

    public Mono<Void> create(
            String name,
            String mail,
            String firstName,
            String lastName,
            String password,
            Agent agent
    ) {
        return permissionsService.assertHasPermission(agent, CREATE, UserId.create())
                .then(userService.create(
                        UserName.of(name),
                        Mail.of(mail),
                        FirstName.of(firstName),
                        LastName.of(lastName),
                        Password.of(password),
                        agent
                ))
                .then();
    }

    public Mono<Void> updateUserName(String id, long version, String name, Agent agent) {
        UserId userId = UserId.of(id);

        return permissionsService.assertHasPermission(agent, UPDATE_USERNAME, userId)
                .then(userService.updateUserName(userId, Version.of(version), UserName.of(name), agent))
                .then();
    }

    public Mono<Void> renameFirstName(String userId, Long version, String firstName, Agent agent) {
        UserId id = UserId.of(userId);

        return permissionsService.assertHasPermission(agent, RENAME, id)
                .then(userService.renameFirstName(id, Version.of(version), FirstName.of(firstName), agent))
                .then();
    }

    public Mono<Void> renameLastName(String userId, Long version, String lastName, Agent agent) {
        UserId id = UserId.of(userId);

        return permissionsService.assertHasPermission(agent, RENAME, id)
                .then(userService.renameLastName(id, Version.of(version), LastName.of(lastName), agent))
                .then();
    }

    public Mono<Void> changePassword(String userId, Long version, String password, Agent agent) {
        UserId id = UserId.of(userId);

        return permissionsService.assertHasPermission(agent, CHANGE_PASSWORD, id)
                .then(userService.changePassword(id, Version.of(version), Password.of(password), agent))
                .then();
    }

    public Mono<Void> remove(String id, long version, Agent agent) {
        UserId userId = UserId.of(id);

        return permissionsService.assertHasPermission(agent, REMOVE, userId)
                .then(userService.remove(userId, Version.of(version), agent))
                .then();
    }

    public Mono<LoginUserResponse> loginByUserName(String userName, CharSequence password) {
        return userLookupRepo.findUserIdByName(UserName.of(userName))
                .flatMap(userId -> userService.login(userId, Password.withoutValidation(password))
                        .map(token -> LoginUserResponse.builder()
                                .token(token.getValue())
                                .userId(userId.getValue())
                                .build()
                        ));
    }

    public Mono<LoginUserResponse> loginByMail(String mail, CharSequence password) {
        return userLookupRepo.findUserIdByMail(Mail.of(mail))
                .flatMap(userId -> userService.login(userId, Password.withoutValidation(password))
                        .map(token -> LoginUserResponse.builder()
                                .token(token.getValue())
                                .userId(userId.getValue())
                                .build()
                        ));
    }

}
