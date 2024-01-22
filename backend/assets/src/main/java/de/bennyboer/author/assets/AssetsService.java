package de.bennyboer.author.assets;

import de.bennyboer.author.assets.create.CreateCmd;
import de.bennyboer.author.assets.remove.RemoveCmd;
import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.EventSourcingService;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.author.eventsourcing.aggregate.AggregateService;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import reactor.core.publisher.Mono;

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

    public Mono<AggregateIdAndVersion<AssetId>> create(Content content, Agent agent) {
        AssetId id = AssetId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(content))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> remove(AssetId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, RemoveCmd.of())
                .flatMap(v -> collapseEvents(id, v, agent));
    }

    public Mono<Version> removeLatest(AssetId id, Agent agent) {
        return dispatchCommandToLatest(id, agent, RemoveCmd.of())
                .flatMap(v -> collapseEvents(id, v, agent));
    }

    @Override
    protected AggregateId toAggregateId(AssetId assetId) {
        return AggregateId.of(assetId.getValue());
    }

    @Override
    protected boolean isRemoved(Asset asset) {
        return asset.isRemoved();
    }

}
