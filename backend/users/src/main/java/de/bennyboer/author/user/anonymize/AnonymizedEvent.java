package de.bennyboer.author.user.anonymize;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnonymizedEvent implements Event {

    private static final Version VERSION = Version.zero();

    public static AnonymizedEvent of() {
        return new AnonymizedEvent();
    }

    @Override
    public EventName getEventName() {
        return UserEvent.ANONYMIZED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
