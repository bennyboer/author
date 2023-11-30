package de.bennyboer.eventsourcing.api.event;

import de.bennyboer.eventsourcing.api.Version;
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
    public EventName getName() {
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
