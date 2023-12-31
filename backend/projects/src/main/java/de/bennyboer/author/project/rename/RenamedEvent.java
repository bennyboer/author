package de.bennyboer.author.project.rename;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.project.ProjectEvent;
import de.bennyboer.author.project.ProjectName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RenamedEvent implements Event {

    private static final Version VERSION = Version.zero();

    ProjectName newName;

    public static RenamedEvent of(ProjectName newName) {
        checkNotNull(newName, "New project name must be given");

        return new RenamedEvent(newName);
    }

    @Override
    public EventName getEventName() {
        return ProjectEvent.RENAMED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
