package de.bennyboer.author.assets;

import de.bennyboer.author.eventsourcing.aggregate.Aggregate;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("ASSET");

    AssetId id;

    Content content;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static Asset init() {
        return new Asset(null, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        return null; // TODO CreateCmd, RemoveCmd, SnapshotCmd
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        return null;
    }

    public boolean isRemoved() {
        return getRemovedAt().isPresent();
    }

    private Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

}
