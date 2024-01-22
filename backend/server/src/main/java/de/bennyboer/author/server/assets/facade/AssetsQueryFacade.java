package de.bennyboer.author.server.assets.facade;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.AssetsService;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.assets.api.AssetDTO;
import de.bennyboer.author.server.assets.permissions.AssetsPermissionsService;
import de.bennyboer.author.server.assets.transformer.AssetTransformer;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.assets.permissions.AssetAction.READ;

@AllArgsConstructor
public class AssetsQueryFacade {

    private final AssetsService assetsService;

    private final AssetsPermissionsService permissionsService;

    public Mono<AssetDTO> getAsset(String id, Agent agent) {
        AssetId assetId = AssetId.of(id);

        return permissionsService.assertHasPermission(agent, READ, assetId)
                .then(assetsService.get(assetId))
                .map(AssetTransformer::toApi);
    }
    
}
