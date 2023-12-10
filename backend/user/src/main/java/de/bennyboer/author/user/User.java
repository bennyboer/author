package de.bennyboer.author.user;

import de.bennyboer.author.user.commands.CreateCmd;
import de.bennyboer.author.user.commands.RemoveCmd;
import de.bennyboer.author.user.commands.RenameCmd;
import de.bennyboer.author.user.events.CreatedEvent;
import de.bennyboer.author.user.events.RemovedEvent;
import de.bennyboer.author.user.events.RenamedEvent;
import de.bennyboer.author.user.events.SnapshottedEvent;
import de.bennyboer.common.UserId;
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
public class User implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("USER");

    UserId id;

    Version version;

    UserName name;

    Instant createdAt;

    @Nullable
    Instant removedAt;

    public static User init() {
        return new User(null, null, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        var isInitialized = Optional.ofNullable(id).isPresent();
        var isCreateCmd = cmd instanceof CreateCmd;
        if (!isInitialized && !isCreateCmd) {
            throw new IllegalStateException(
                    "User must be initialized with CreateCmd before applying other commands"
            );
        }

        if (isRemoved()) {
            throw new IllegalStateException("Cannot apply command to removed User");
        }

        if (!isAllowedAgent(agent)) {
            throw new IllegalStateException("Agent is not allowed to apply command");
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
        var updatedUser = switch (event) {
            case SnapshottedEvent e -> withId(UserId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withCreatedAt(e.getCreatedAt());
            case CreatedEvent e -> withId(UserId.of(metadata.getAggregateId().getValue()))
                    .withName(e.getName())
                    .withCreatedAt(metadata.getDate());
            case RenamedEvent e -> withName(e.getNewName());
            case RemovedEvent ignored -> withRemovedAt(metadata.getDate());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };

        return updatedUser.withVersion(metadata.getAggregateVersion());
    }

    public boolean isRemoved() {
        return getRemovedAt().isPresent();
    }

    private Optional<Instant> getRemovedAt() {
        return Optional.ofNullable(removedAt);
    }

    private boolean isAllowedAgent(Agent agent) {
        if (agent.isSystem()) {
            return true;
        }

        return agent.getUserId()
                .map(id -> id.equals(this.id))
                .orElse(false);
    }

}
