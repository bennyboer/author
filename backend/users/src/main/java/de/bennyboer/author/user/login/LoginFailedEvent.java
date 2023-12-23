package de.bennyboer.author.user.login;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginFailedEvent implements Event {

    public static final EventName NAME = EventName.of("LOGIN_FAILED");

    public static final Version VERSION = Version.zero();

    public static LoginFailedEvent of() {
        return new LoginFailedEvent();
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
