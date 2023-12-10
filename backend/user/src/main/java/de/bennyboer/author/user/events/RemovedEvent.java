package de.bennyboer.author.user.events;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemovedEvent implements Event {

    public static final EventName NAME = EventName.of("REMOVED");

    public static final Version VERSION = Version.zero();

    public static RemovedEvent of() {
        return new RemovedEvent();
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
