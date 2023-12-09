package de.bennyboer.author.project.model;

import de.bennyboer.eventsourcing.api.aggregate.Aggregate;
import de.bennyboer.eventsourcing.api.aggregate.ApplyCommandResult;
import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Project implements Aggregate {

    ProjectId id;

    long version;

    ProjectName name;

    Instant createdAt;

    public static Project init() {
        return new Project(null, 0L, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd) {
        // TODO Create project service tests
        // TODO Preventing applying events if not initialized

        return null; // TODO CreateCmd
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        return null; // TODO CreatedEvent
    }

}
