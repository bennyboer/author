package de.bennyboer.author.server.assets.facade;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.assets.permissions.AssetsPermissionsService;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class AssetsPermissionsFacade {

    private final AssetsPermissionsService permissionsService;

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AssetId assetId) {
        return permissionsService.hasPermissionToReceiveEvents(agent, assetId);
    }

    public Mono<Void> addPermissionToCreateAssetsForNewUser(UserId userId) {
        return permissionsService.addPermissionToCreateAssetsForNewUser(userId);
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, AssetId assetId) {
        return permissionsService.addPermissionsForCreator(userId, assetId);
    }

    public Mono<Void> removeCreateCreatePermissionForUser(UserId userId) {
        return permissionsService.removeCreatePermissionForUser(userId);
    }

    public Mono<Void> removePermissionsForAsset(AssetId assetId) {
        return permissionsService.removePermissionsForAsset(assetId);
    }

}
