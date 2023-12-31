package de.bennyboer.author.project;

import de.bennyboer.author.eventsourcing.event.EventName;

public enum ProjectEvent {

    CREATED,
    REMOVED,
    RENAMED,
    SNAPSHOTTED;

    public EventName getName() {
        return EventName.of(name());
    }

    public static ProjectEvent fromName(EventName name) {
        return valueOf(name.getValue());
    }

}
