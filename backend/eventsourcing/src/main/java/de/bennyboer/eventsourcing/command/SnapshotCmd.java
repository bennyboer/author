package de.bennyboer.eventsourcing.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * A predefined command to trigger a snapshot.
 * All aggregates that wish to be snapshotted must handle this command.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshotCmd implements Command {

    public static SnapshotCmd of() {
        return new SnapshotCmd();
    }

}
