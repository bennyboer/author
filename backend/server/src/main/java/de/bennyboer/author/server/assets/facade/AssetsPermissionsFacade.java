package de.bennyboer.author.server.assets.facade;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.assets.permissions.AssetsPermissionsService;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class AssetsPermissionsFacade {

    private final AssetsPermissionsService permissionsService;

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, AssetId assetId) {
        return Mono.just(false); // TODO
    }

    // TODO Add permissions when asset is created (by user)

}
