package de.bennyboer.author.server.structure.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.shared.permissions.AggregatePermissionsService;
import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeId;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TreePermissionsService extends AggregatePermissionsService<TreeId, TreeAction> {

    public TreePermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        super(permissionsRepo, eventPublisher);
    }

    @Override
    public AggregateType getAggregateType() {
        return Tree.TYPE;
    }

    @Override
    public ResourceId getResourceId(TreeId id) {
        return ResourceId.of(id.getValue());
    }

    @Override
    public Action toAction(TreeAction action) {
        return Action.of(action.name());
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, TreeId treeId) {
        Resource resource = toResource(treeId);

        Set<Permission> permissions = Arrays.stream(TreeAction.values())
                .map(action -> Permission.builder()
                        .user(userId)
                        .isAllowedTo(toAction(action))
                        .on(resource))
                .collect(Collectors.toSet());

        return addPermissions(permissions);
    }

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return removePermissionsByUserId(userId);
    }

}
