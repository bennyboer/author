package de.bennyboer.author.project;

import de.bennyboer.author.project.create.CreateCmd;
import de.bennyboer.author.project.remove.RemoveCmd;
import de.bennyboer.author.project.rename.RenameCmd;
import de.bennyboer.author.project.create.CreatedEvent;
import de.bennyboer.author.project.remove.RemovedEvent;
import de.bennyboer.author.project.rename.RenamedEvent;
import de.bennyboer.author.project.snapshot.SnapshottedEvent;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.Aggregate;
import de.bennyboer.eventsourcing.aggregate.AggregateType;
import de.bennyboer.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.command.Command;
import de.bennyboer.eventsourcing.command.SnapshotCmd;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import jakarta.annotation.Nullable;
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

    Version version;

    ProjectName name;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static Project init() {
        return new Project(null, null, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
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

        // TODO Check if agent is allowed to apply command

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

        return updatedProject.withVersion(metadata.getAggregateVersion());
    }

    public boolean isRemoved() {
        return getRemovedAt().isPresent();
    }

    private Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

}