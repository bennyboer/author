package de.bennyboer.eventsourcing.sample;

import de.bennyboer.eventsourcing.aggregate.Aggregate;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.command.Command;
import de.bennyboer.eventsourcing.command.SnapshotCmd;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.sample.commands.CreateCmd;
import de.bennyboer.eventsourcing.sample.commands.DeleteCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateDescriptionCmd;
import de.bennyboer.eventsourcing.sample.commands.UpdateTitleCmd;
import de.bennyboer.eventsourcing.sample.events.*;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Value;
import lombok.With;

import java.time.Instant;

@Value
@With(AccessLevel.PRIVATE)
public class SampleAggregate implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("SAMPLE");

    String id;

    String title;

    String description;

    @Nullable
    Instant deletedAt;

    public static SampleAggregate init() {
        return new SampleAggregate(null, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent ignoredAgent) {
        if (deletedAt != null) {
            throw new IllegalStateException("Cannot apply command to deleted aggregate");
        }

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(this));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent2.of(c));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            case UpdateTitleCmd c -> ApplyCommandResult.of(TitleUpdatedEvent.of(c));
            case UpdateDescriptionCmd c -> ApplyCommandResult.of(DescriptionUpdatedEvent.of(c));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        return switch (event) {
            case SnapshottedEvent e -> withId(metadata.getAggregateId().getValue())
                    .withTitle(e.getTitle())
                    .withDescription(e.getDescription())
                    .withDeletedAt(e.getDeletedAt());
            case CreatedEvent2 e -> withId(metadata.getAggregateId().getValue())
                    .withTitle(e.getTitle())
                    .withDescription(e.getDescription())
                    .withDeletedAt(e.getDeletedAt());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case TitleUpdatedEvent e -> withTitle(e.getTitle());
            case DescriptionUpdatedEvent e -> withDescription(e.getDescription());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

}
