package de.bennyboer.author.server.assets.persistence.lookup;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.Owner;
import de.bennyboer.author.eventsourcing.persistence.readmodel.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;

public class InMemoryAssetLookupRepo extends InMemoryEventSourcingReadModelRepo<AssetId, LookupAsset>
        implements AssetLookupRepo {

    @Override
    public Flux<LookupAsset> findAssetsOwnedBy(Owner owner) {
        return Flux.fromIterable(lookup.values())
                .filter(lookupAsset -> lookupAsset.getOwner().equals(owner));
    }

    @Override
    protected AssetId getId(LookupAsset readModel) {
        return readModel.getId();
    }

}