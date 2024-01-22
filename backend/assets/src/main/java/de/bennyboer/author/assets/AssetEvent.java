package de.bennyboer.author.assets;

import de.bennyboer.author.eventsourcing.event.EventName;

public enum AssetEvent {

    CREATED,
    REMOVED,
    SNAPSHOTTED;

    public EventName getName() {
        return EventName.of(name());
    }

    public static AssetEvent fromName(EventName name) {
        return valueOf(name.getValue());
    }

}
