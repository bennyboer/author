package de.bennyboer.author.user;

import de.bennyboer.author.eventsourcing.event.EventName;

public enum UserEvent {

    CREATED,
    LOGGED_IN,
    LOGIN_FAILED,
    REMOVED,
    SNAPSHOTTED,
    RENAMED;

    public EventName getName() {
        return EventName.of(name());
    }

    public static UserEvent fromName(EventName name) {
        return valueOf(name.getValue());
    }

}
