package de.bennyboer.author.eventsourcing.event;

import de.bennyboer.author.eventsourcing.Version;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class AbstractEvent implements Event {

    private final EventName name;

    private final Version version;

    protected AbstractEvent(EventName name, Version version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public EventName getEventName() {
        return name;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public boolean isSnapshot() {
        return false;
    }

}
