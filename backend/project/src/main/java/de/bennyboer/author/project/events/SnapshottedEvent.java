package de.bennyboer.author.project.events;

import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectName;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.Event;
import de.bennyboer.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event {

    public static final EventName NAME = EventName.of("SNAPSHOTTED");

    public static final Version VERSION = Version.zero();

    ProjectName name;

    Instant createdAt;

    public static SnapshottedEvent of(Project project) {
        return new SnapshottedEvent(project.getName(), project.getCreatedAt());
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
