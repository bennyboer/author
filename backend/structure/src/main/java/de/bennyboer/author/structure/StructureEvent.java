package de.bennyboer.author.structure;

import de.bennyboer.author.eventsourcing.event.EventName;

public enum StructureEvent {

    CREATED,
    REMOVED,
    SNAPSHOTTED,
    NODE_ADDED,
    NODE_REMOVED,
    NODE_RENAMED,
    NODES_SWAPPED,
    NODE_TOGGLED;

    public EventName getName() {
        return EventName.of(name());
    }

    public static StructureEvent fromName(EventName name) {
        return valueOf(name.getValue());
    }

}
