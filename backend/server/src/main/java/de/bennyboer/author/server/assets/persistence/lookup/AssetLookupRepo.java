package de.bennyboer.author.server.assets.persistence.lookup;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.Owner;
import de.bennyboer.author.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Flux;

public interface AssetLookupRepo extends EventSourcingReadModelRepo<AssetId, LookupAsset> {

    Flux<LookupAsset> findAssetsOwnedBy(Owner owner);

}
