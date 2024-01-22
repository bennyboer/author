package de.bennyboer.author.server.assets.messaging;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.AssetEvent;
import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.server.assets.facade.AssetsPermissionsFacade;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class AssetRemovedRemovePermissionsMsgListener implements AggregateEventMessageListener {

    private final AssetsPermissionsFacade permissionsFacade;

    @Override
    public AggregateType aggregateType() {
        return Asset.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(AssetEvent.REMOVED.getName());
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        AssetId assetId = AssetId.of(message.getAggregateId());

        return permissionsFacade.removePermissionsForAsset(assetId);
    }

}
