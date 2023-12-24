package de.bennyboer.author.server.users.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.shared.permissions.AggregatePermissionsService;
import de.bennyboer.author.user.User;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.author.server.users.permissions.UserAction.READ;

public class UserPermissionsService extends AggregatePermissionsService<UserId, UserAction> {

    public UserPermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        super(permissionsRepo, eventPublisher);
    }

    @Override
    public AggregateType getAggregateType() {
        return User.TYPE;
    }

    @Override
    public ResourceId getResourceId(UserId id) {
        return ResourceId.of(id.getValue());
    }

    @Override
    public Action toAction(UserAction action) {
        return Action.of(action.name());
    }

    public Mono<Void> addPermissionsForUser(UserId userId) {
        Resource resource = toResource(userId);

        Set<Permission> permissions = Arrays.stream(UserAction.values())
                .map(action -> Permission.builder()
                        .user(userId)
                        .isAllowedTo(toAction(action))
                        .on(resource))
                .collect(Collectors.toSet());

        return addPermissions(permissions);
    }

    public Mono<Void> removePermissionsForUser(UserId userId) {
        Resource resource = toResource(userId);

        return removePermissionsByUserId(userId)
                .then(removePermissionsByResource(resource));
    }

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, UserId userId) {
        return hasPermission(agent, READ, userId);
    }

}
