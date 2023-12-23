package de.bennyboer.author.server.users.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.server.users.permissions.UserPermissionsService;
import de.bennyboer.author.server.users.transformer.UserTransformer;
import de.bennyboer.author.user.UserService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.users.permissions.UserAction.READ;

@Value
@AllArgsConstructor
public class UsersQueryFacade {

    UserService userService;

    UserPermissionsService permissionsService;

    public Mono<UserDTO> getUser(String id, Agent agent) {
        UserId userId = UserId.of(id);

        return permissionsService.assertHasPermission(agent, READ, userId)
                .then(userService.get(userId))
                .map(UserTransformer::toApi);
    }

}
