package de.bennyboer.author.server.assets.persistence.lookup;

import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.assets.Owner;
import de.bennyboer.author.eventsourcing.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LookupAsset {

    AssetId id;

    Version version;

    Owner owner;

    public static LookupAsset of(AssetId id, Version version, Owner owner) {
        checkNotNull(id, "Asset ID must be given");
        checkNotNull(version, "Version must be given");
        checkNotNull(owner, "Owner must be given");

        return new LookupAsset(id, version, owner);
    }

}
