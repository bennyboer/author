package de.bennyboer.author.server.assets;

import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.assets.persistence.lookup.AssetLookupRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AssetsConfig {

    EventSourcingRepo eventSourcingRepo;

    PermissionsRepo permissionsRepo;

    AssetLookupRepo assetLookupRepo;

}
