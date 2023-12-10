package de.bennyboer.author.project;

import de.bennyboer.author.project.commands.CreateCmd;
import de.bennyboer.author.project.commands.RemoveCmd;
import de.bennyboer.author.project.commands.RenameCmd;
import de.bennyboer.author.project.events.CreatedEvent;
import de.bennyboer.author.project.events.RemovedEvent;
import de.bennyboer.author.project.events.RenamedEvent;
import de.bennyboer.author.project.events.SnapshottedEvent;
import de.bennyboer.eventsourcing.aggregate.Aggregate;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.command.Command;
import de.bennyboer.eventsourcing.command.SnapshotCmd;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Project implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("PROJECT");

    ProjectId id;

    long version;

    ProjectName name;

    Instant createdAt;

    Instant removedAt;

    public static Project init() {
        return new Project(null, 0L, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd) {
        var isInitialized = Optional.ofNullable(id).isPresent();
        var isCreateCmd = cmd instanceof CreateCmd;
        if (!isInitialized && !isCreateCmd) {
            throw new IllegalStateException(
                    "Project must be initialized with CreateCmd before applying other commands"
            );
        }

        if (isRemoved()) {
            throw new IllegalStateException("Cannot apply command to removed Project");
        }

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(this));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(c));
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c));
            case RemoveCmd ignored -> ApplyCommandResult.of(RemovedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var updatedProject = switch (event) {
            case SnapshottedEvent e -> withId(ProjectId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withCreatedAt(e.getCreatedAt());
            case CreatedEvent e -> withId(ProjectId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withCreatedAt(metadata.getDate());
            case RenamedEvent e -> withName(e.getNewName());
            case RemovedEvent ignored -> withRemovedAt(metadata.getDate());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };

        return updatedProject.withVersion(metadata.getAggregateVersion().getValue());
    }

    public boolean isRemoved() {
        return Optional.ofNullable(removedAt).isPresent();
    }

}
