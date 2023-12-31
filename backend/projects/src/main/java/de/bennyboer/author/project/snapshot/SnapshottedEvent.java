package de.bennyboer.author.project.snapshot;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.SnapshotEvent;
import de.bennyboer.author.project.ProjectEvent;
import de.bennyboer.author.project.ProjectName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnapshottedEvent implements Event, SnapshotEvent {

    private static final Version VERSION = Version.zero();

    ProjectName name;

    Instant createdAt;

    public static SnapshottedEvent of(ProjectName name, Instant createdAt) {
        checkNotNull(name, "Project name must be given");
        checkNotNull(createdAt, "Created at must be given");

        return new SnapshottedEvent(name, createdAt);
    }

    @Override
    public EventName getEventName() {
        return ProjectEvent.SNAPSHOTTED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
