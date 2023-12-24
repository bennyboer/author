package de.bennyboer.author.server.structure.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.server.structure.permissions.TreePermissionsService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class TreePermissionsFacade {

    TreePermissionsService permissionsService;

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return permissionsService.removePermissionsForUser(userId);
    }
    
}
