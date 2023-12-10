package de.bennyboer.author.user.events;

import de.bennyboer.author.user.User;
import de.bennyboer.author.user.UserName;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    UserName name;

    Instant createdAt;

    public static SnapshottedEvent of(User user) {
        return new SnapshottedEvent(user.getName(), user.getCreatedAt());
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
