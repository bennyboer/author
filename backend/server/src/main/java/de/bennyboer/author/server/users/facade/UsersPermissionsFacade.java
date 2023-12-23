package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.users.permissions.UserPermissionsService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class UsersPermissionsFacade {

    UserPermissionsService permissionsService;

    public Mono<Void> addPermissionsForUser(UserId userId) {
        return permissionsService.addPermissionsForUser(userId);
    }

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return permissionsService.removePermissionsForUser(userId);
    }

}
