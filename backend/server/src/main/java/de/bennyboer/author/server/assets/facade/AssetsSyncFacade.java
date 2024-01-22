package de.bennyboer.author.server.assets.facade;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.AssetsService;
import de.bennyboer.author.server.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.author.server.assets.persistence.lookup.LookupAsset;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class AssetsSyncFacade {

    private final AssetsService assetsService;

    private final AssetLookupRepo lookupRepo;

    public Mono<Void> updateInLookup(AssetId assetId) {
        return assetsService.get(assetId)
                .map(this::toLookupAsset)
                .flatMap(lookupRepo::update);
    }

    public Mono<Void> removeFromLookup(AssetId assetId) {
        return lookupRepo.remove(assetId);
    }

    private LookupAsset toLookupAsset(Asset asset) {
        return LookupAsset.of(
                asset.getId(),
                asset.getVersion(),
                asset.getOwner()
        );
    }

}
