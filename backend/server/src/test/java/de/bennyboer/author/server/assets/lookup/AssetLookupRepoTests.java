package de.bennyboer.author.server.assets.lookup;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.Owner;
import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.server.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.author.server.assets.persistence.lookup.LookupAsset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AssetLookupRepoTests {

    private final AssetLookupRepo repo = createRepo();

    protected abstract AssetLookupRepo createRepo();

    @Test
    void shouldInsertAsset() {
        // given: an asset to insert
        var asset = LookupAsset.of(
                AssetId.of("ASSET_ID"),
                Version.of(3L),
                Owner.of(UserId.of("USER_ID"))
        );

        // when: the asset is updated
        repo.update(asset).block();

        // then: the asset is updated
        var assets = repo.findAssetsOwnedBy(Owner.of(UserId.of("USER_ID")))
                .collectList()
                .block();
        var actualAsset = assets.get(0);
        assertThat(actualAsset.getId()).isEqualTo(AssetId.of("ASSET_ID"));
        assertThat(actualAsset.getVersion()).isEqualTo(Version.of(3L));
        assertThat(actualAsset.getOwner()).isEqualTo(Owner.of(UserId.of("USER_ID")));
    }

    @Test
    void shouldUpdateMultipleAssetsForDifferentOwners() {
        Owner owner1 = Owner.of(UserId.of("USER_ID_1"));
        Owner owner2 = Owner.of(UserId.of("USER_ID_2"));

        // given: multiple assets for different owners
        var asset1 = LookupAsset.of(
                AssetId.of("ASSET_ID_1"),
                Version.zero(),
                owner1
        );
        var asset2 = LookupAsset.of(
                AssetId.of("ASSET_ID_2"),
                Version.zero(),
                owner2
        );
        var asset3 = LookupAsset.of(
                AssetId.of("ASSET_ID_3"),
                Version.zero(),
                owner1
        );

        // when: the assets are updated
        repo.update(asset1).block();
        repo.update(asset2).block();
        repo.update(asset3).block();

        // then: the asset IDs can be retrieved for each owner
        var assetIds1 = repo.findAssetsOwnedBy(owner1).map(LookupAsset::getId).collectList().block();
        assertThat(assetIds1.size()).isEqualTo(2);
        assertThat(assetIds1).contains(AssetId.of("ASSET_ID_1"), AssetId.of("ASSET_ID_3"));

        var assetIds2 = repo.findAssetsOwnedBy(owner2).map(LookupAsset::getId).collectList().block();
        assertThat(assetIds2).contains(AssetId.of("ASSET_ID_2"));
    }

    @Test
    void shouldRemoveAsset() {
        // given: an asset to insert
        var asset = LookupAsset.of(
                AssetId.of("ASSET_ID"),
                Version.zero(),
                Owner.of(UserId.of("USER_ID"))
        );

        // when: the asset is updated
        repo.update(asset).block();

        // then: the asset is updated
        var assetIds = repo.findAssetsOwnedBy(Owner.of(UserId.of("USER_ID")))
                .map(LookupAsset::getId)
                .collectList()
                .block();
        assertThat(assetIds).containsExactly(AssetId.of("ASSET_ID"));

        // when: the asset is removed
        repo.remove(asset.getId()).block();

        // then: the asset is removed
        assetIds = repo.findAssetsOwnedBy(Owner.of(UserId.of("USER_ID"))).map(LookupAsset::getId).collectList().block();
        assertThat(assetIds).isEmpty();
    }

}
