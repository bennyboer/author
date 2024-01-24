package de.bennyboer.author.server.assets.facade;

import de.bennyboer.author.assets.*;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.server.assets.permissions.AssetsPermissionsService;
import de.bennyboer.author.server.assets.persistence.lookup.AssetLookupRepo;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.assets.permissions.AssetAction.CREATE;

@AllArgsConstructor
public class AssetsCommandFacade {

    private final AssetsService assetsService;

    private final AssetsPermissionsService permissionsService;

    private final AssetLookupRepo lookupRepo;

    public Mono<AssetId> create(String content, String contentType, Agent agent) {
        ContentType type = ContentType.fromMimeType(contentType);
        Content cnt = Content.of(content, type);

        return permissionsService.assertHasPermission(agent, CREATE)
                .then(assetsService.create(cnt, agent))
                .map(AggregateIdAndVersion::getId);
    }

    public Mono<Void> remove(String assetId, long version, Agent agent) {
        return assetsService.remove(AssetId.of(assetId), Version.of(version), agent).then();
    }

    public Mono<Void> removeLatest(AssetId assetId, Agent agent) {
        return assetsService.removeLatest(assetId, agent).then();
    }

    public Mono<Void> removeOwnedAssets(UserId userId, Agent agent) {
        return lookupRepo.findAssetsOwnedBy(Owner.of(userId))
                .flatMap(asset -> assetsService.removeLatest(asset.getId(), agent))
                .then();
    }

}
