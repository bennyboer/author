package de.bennyboer.author.user.events;

import de.bennyboer.author.user.UserName;
import de.bennyboer.author.user.commands.CreateCmd;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    UserName name;

    public static CreatedEvent of(CreateCmd cmd) {
        return new CreatedEvent(cmd.getName());
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
