package de.bennyboer.author.user.login;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggedInEvent implements Event {

    private static final Version VERSION = Version.zero();

    public static LoggedInEvent of() {
        return new LoggedInEvent();
    }

    @Override
    public EventName getEventName() {
        return UserEvent.LOGGED_IN.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
