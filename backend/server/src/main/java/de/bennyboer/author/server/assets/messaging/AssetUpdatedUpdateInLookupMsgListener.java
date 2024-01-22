package de.bennyboer.author.server.assets.messaging;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.AssetEvent;
import de.bennyboer.author.assets.AssetId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.server.assets.facade.AssetsSyncFacade;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class AssetUpdatedUpdateInLookupMsgListener implements AggregateEventMessageListener {

    private final AssetsSyncFacade syncFacade;

    @Override
    public AggregateType aggregateType() {
        return Asset.TYPE;
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        AssetId assetId = AssetId.of(message.getAggregateId());
        boolean isRemoved = message.getEventName().equals(AssetEvent.REMOVED.getName().getValue());
        if (isRemoved) {
            return syncFacade.removeFromLookup(assetId);
        }

        return syncFacade.updateInLookup(assetId);
    }

}
