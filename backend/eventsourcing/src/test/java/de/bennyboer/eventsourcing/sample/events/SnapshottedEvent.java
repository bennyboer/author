package de.bennyboer.eventsourcing.sample.events;

import de.bennyboer.eventsourcing.api.event.AbstractEvent;
import de.bennyboer.eventsourcing.api.event.EventName;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.event.SnapshotEvent;
import de.bennyboer.eventsourcing.sample.SampleAggregate;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Instant;

@Value
@EqualsAndHashCode(callSuper = true)
public class SnapshottedEvent extends AbstractEvent implements SnapshotEvent {

    private static final EventName NAME = EventName.of("SNAPSHOTTED");

    private static final Version VERSION = Version.zero();

    String title;

    String description;

    Instant deletedAt;

    private SnapshottedEvent(String title, String description, Instant deletedAt) {
        super(NAME, VERSION);

        this.title = title;
        this.description = description;
        this.deletedAt = deletedAt;
    }

    public static SnapshottedEvent of(SampleAggregate aggregate) {
        return new SnapshottedEvent(aggregate.getTitle(), aggregate.getDescription(), aggregate.getDeletedAt());
    }

}
