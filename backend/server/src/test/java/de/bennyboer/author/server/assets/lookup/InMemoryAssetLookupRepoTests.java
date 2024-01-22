package de.bennyboer.author.server.assets.lookup;

import de.bennyboer.author.server.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.author.server.assets.persistence.lookup.InMemoryAssetLookupRepo;

public class InMemoryAssetLookupRepoTests extends AssetLookupRepoTests {

    @Override
    protected AssetLookupRepo createRepo() {
        return new InMemoryAssetLookupRepo();
    }

}
