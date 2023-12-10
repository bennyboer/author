package de.bennyboer.author.project.events;

import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.project.commands.RenameCmd;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenamedEvent implements Event {

    public static final EventName NAME = EventName.of("RENAMED");

    public static final Version VERSION = Version.zero();

    ProjectName newName;

    public static RenamedEvent of(RenameCmd cmd) {
        return new RenamedEvent(cmd.getNewName());
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
