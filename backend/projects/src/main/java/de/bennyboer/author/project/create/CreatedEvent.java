package de.bennyboer.author.project.create;

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
public class CreatedEvent implements Event {

    private static final Version VERSION = Version.zero();

    ProjectName name;

    public static CreatedEvent of(ProjectName name) {
        checkNotNull(name, "Project name must be given");

        return new CreatedEvent(name);
    }

    @Override
    public EventName getEventName() {
        return ProjectEvent.CREATED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
