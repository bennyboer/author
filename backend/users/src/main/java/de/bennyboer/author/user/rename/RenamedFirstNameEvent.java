package de.bennyboer.author.user.rename;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.FirstName;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenamedFirstNameEvent implements Event {

    private static final Version VERSION = Version.zero();

    FirstName firstName;

    public static RenamedFirstNameEvent of(FirstName firstName) {
        checkNotNull(firstName, "First name must be given");

        return new RenamedFirstNameEvent(firstName);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.RENAMED_FIRST_NAME.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
