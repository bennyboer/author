package de.bennyboer.author.server.assets.lookup;

import de.bennyboer.author.server.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.author.server.assets.persistence.lookup.SQLiteAssetLookupRepo;

public class SQLiteAssetLookupRepoTests extends AssetLookupRepoTests {

    @Override
    protected AssetLookupRepo createRepo() {
        return new SQLiteAssetLookupRepo(true);
    }

}
