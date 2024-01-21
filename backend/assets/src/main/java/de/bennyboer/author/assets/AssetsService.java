package de.bennyboer.author.assets;

import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.EventSourcingService;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateService;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;

import java.util.List;

public class AssetsService extends AggregateService<Asset, AssetId> {

    public AssetsService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Asset.TYPE,
                Asset.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    // TODO Collapse events when removing asset to avoid having to store the removed assets content

    @Override
    protected AggregateId toAggregateId(AssetId assetId) {
        return AggregateId.of(assetId.getValue());
    }

    @Override
    protected boolean isRemoved(Asset asset) {
        return asset.isRemoved();
    }

}
