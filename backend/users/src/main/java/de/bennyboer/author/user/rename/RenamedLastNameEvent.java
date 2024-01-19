package de.bennyboer.author.user.rename;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.LastName;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenamedLastNameEvent implements Event {

    private static final Version VERSION = Version.zero();

    LastName lastName;

    public static RenamedLastNameEvent of(LastName lastName) {
        checkNotNull(lastName, "Last name must be given");

        return new RenamedLastNameEvent(lastName);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.RENAMED_LAST_NAME.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
