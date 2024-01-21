package de.bennyboer.author.server.assets.permissions;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.Action;
import de.bennyboer.author.permissions.PermissionsEventPublisher;
import de.bennyboer.author.permissions.ResourceId;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.server.shared.permissions.AggregatePermissionsService;

public class AssetsPermissionsService extends AggregatePermissionsService<AssetId, AssetAction> {

    public AssetsPermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        super(permissionsRepo, eventPublisher);
    }

    @Override
    public AggregateType getAggregateType() {
        return Asset.TYPE;
    }

    @Override
    public ResourceId getResourceId(AssetId id) {
        return ResourceId.of(id.getValue());
    }

    @Override
    public AssetId toId(ResourceId resourceId) {
        return AssetId.of(resourceId.getValue());
    }

    @Override
    public Action toAction(AssetAction action) {
        return Action.of(action.name());
    }

    // TODO Methods to check/add/remove permissions

}
