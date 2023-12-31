package de.bennyboer.author.user.rename;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.UserEvent;
import de.bennyboer.author.user.UserName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenamedEvent implements Event {

    private static final Version VERSION = Version.zero();

    UserName newName;

    public static RenamedEvent of(UserName newName) {
        checkNotNull(newName, "New name must be given");

        return new RenamedEvent(newName);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.RENAMED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
