package de.bennyboer.author.user.login;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggedInEvent implements Event {

    public static final EventName NAME = EventName.of("LOGGED_IN");

    public static final Version VERSION = Version.zero();

    public static LoggedInEvent of() {
        return new LoggedInEvent();
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
