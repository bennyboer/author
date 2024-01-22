package de.bennyboer.author.assets;

import de.bennyboer.author.assets.create.CreateCmd;
import de.bennyboer.author.assets.create.CreatedEvent;
import de.bennyboer.author.assets.remove.RemoveCmd;
import de.bennyboer.author.assets.remove.RemovedEvent;
import de.bennyboer.author.assets.snapshot.SnapshottedEvent;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.aggregate.Aggregate;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.eventsourcing.command.SnapshotCmd;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Asset implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("ASSET");

    AssetId id;

    Version version;

    @Nullable
    Content content;

    Owner owner;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static Asset init() {
        return new Asset(null, null, null, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        var isInitialized = Optional.ofNullable(id).isPresent();
        var isCreateCmd = cmd instanceof CreateCmd;
        if (!isInitialized && !isCreateCmd) {
            throw new IllegalStateException(
                    "Asset must be initialized with CreateCmd before applying other commands"
            );
        }

        if (isRemoved()) {
            var exceptions = Set.of(SnapshotCmd.class);
            if (!exceptions.contains(cmd.getClass())) {
                throw new IllegalStateException("Cannot apply command to removed Asset");
            }
        }

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getContent().orElse(null),
                    getOwner(),
                    getCreatedAt(),
                    getRemovedAt().orElse(null)
            ));
            case CreateCmd c -> applyCreateCmd(c, agent);
            case RemoveCmd ignored -> ApplyCommandResult.of(RemovedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Asset apply(Event event, EventMetadata metadata) {
        var updatedProject = switch (event) {
            case SnapshottedEvent e -> withId(AssetId.of(metadata.getAggregateId().getValue()))
                    .withContent(e.getContent().orElse(null))
                    .withOwner(e.getOwner())
                    .withCreatedAt(e.getCreatedAt())
                    .withRemovedAt(e.getRemovedAt().orElse(null));
            case CreatedEvent e -> withId(AssetId.of(metadata.getAggregateId().getValue()))
                    .withContent(e.getContent())
                    .withOwner(Owner.of(metadata.getAgent().getUserId().orElseThrow()))
                    .withCreatedAt(metadata.getDate());
            case RemovedEvent ignored -> withRemovedAt(metadata.getDate())
                    .withContent(null);
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };

        return updatedProject.withVersion(metadata.getAggregateVersion());
    }

    public Optional<Content> getContent() {
        return Optional.ofNullable(content);
    }

    public boolean isRemoved() {
        return getRemovedAt().isPresent();
    }

    private Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

    private ApplyCommandResult applyCreateCmd(CreateCmd c, Agent agent) {
        if (agent.isSystem()) {
            throw new IllegalArgumentException("System agent is not allowed to create assets");
        }
        if (agent.isAnonymous()) {
            throw new IllegalArgumentException("Anonymous agent is not allowed to create assets");
        }

        return ApplyCommandResult.of(CreatedEvent.of(c.getContent()));
    }

}
