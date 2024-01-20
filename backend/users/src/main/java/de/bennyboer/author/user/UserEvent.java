package de.bennyboer.author.user;

import de.bennyboer.author.eventsourcing.event.EventName;

public enum UserEvent {

    CREATED,
    LOGGED_IN,
    LOGIN_FAILED,
    REMOVED,
    SNAPSHOTTED,
    USERNAME_CHANGED,
    MAIL_UPDATE_REQUESTED,
    MAIL_UPDATE_CONFIRMED,
    RENAMED_FIRST_NAME,
    RENAMED_LAST_NAME,
    PASSWORD_CHANGED,
    ANONYMIZED;

    public EventName getName() {
        return EventName.of(name());
    }

    public static UserEvent fromName(EventName name) {
        return valueOf(name.getValue());
    }

}
