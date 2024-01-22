package de.bennyboer.author.server.assets.permissions;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.shared.permissions.AggregatePermissionsService;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.author.server.assets.permissions.AssetAction.CREATE;
import static de.bennyboer.author.server.assets.permissions.AssetAction.READ;

public class AssetsPermissionsService extends AggregatePermissionsService<AssetId, AssetAction> {

    public AssetsPermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        super(permissionsRepo, eventPublisher);
    }

    @Override
    public AggregateType getAggregateType() {
        return Asset.TYPE;
    }

    @Override
    public ResourceId getResourceId(AssetId id) {
        return ResourceId.of(id.getValue());
    }

    @Override
    public AssetId toId(ResourceId resourceId) {
        return AssetId.of(resourceId.getValue());
    }

    @Override
    public Action toAction(AssetAction action) {
        return Action.of(action.name());
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, AssetId assetId) {
        Resource resource = toResource(assetId);

        Set<Permission> permissions = Arrays.stream(AssetAction.values())
                .map(action -> Permission.builder()
                        .user(userId)
                        .isAllowedTo(toAction(action))
                        .on(resource))
                .collect(Collectors.toSet());

        return addPermissions(permissions);
    }

    public Mono<Void> addPermissionToCreateAssetsForNewUser(UserId userId) {
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(toAction(CREATE))
                .on(Resource.ofType(getResourceType()));

        return addPermission(permission);
    }

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AssetId assetId) {
        return hasPermission(agent, READ, assetId);
    }

    public Mono<Void> removeCreatePermissionForUser(UserId userId) {
        Permission permission = Permission.builder()
                .user(userId)
                .isAllowedTo(toAction(CREATE))
                .on(Resource.ofType(getResourceType()));

        return removePermission(permission);
    }

    public Mono<Void> removePermissionsForAsset(AssetId assetId) {
        return removePermissionsByResource(assetId);
    }

}
