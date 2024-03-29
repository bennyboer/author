package de.bennyboer.author.assets.remove;

import de.bennyboer.author.assets.AssetEvent;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemovedEvent implements Event {

    private static final Version VERSION = Version.zero();

    public static RemovedEvent of() {
        return new RemovedEvent();
    }

    @Override
    public EventName getEventName() {
        return AssetEvent.REMOVED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
