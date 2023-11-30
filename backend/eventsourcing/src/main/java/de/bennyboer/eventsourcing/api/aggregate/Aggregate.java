package de.bennyboer.eventsourcing.api.aggregate;

import de.bennyboer.eventsourcing.api.command.Command;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;

/**
 * An aggregate in CQRS/Event Sourcing is the end result of all events that have been applied to it.
 * It can be rebuilt from its events and may emit new events when a command is applied to it.
 */
public interface Aggregate {

    /**
     * Apply a command to the aggregate.
     * This may result in one, multiple or no events being emitted.
     */
    ApplyCommandResult apply(Command cmd);

    Aggregate apply(Event event, EventMetadata metadata);

    /**
     * The amount of events to create a snapshot after.
     * Return -1 to disable snapshots.
     */
    default int getCountOfEventsToSnapshotAfter() {
        return 100;
    }

}
