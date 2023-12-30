package de.bennyboer.author.eventsourcing.sample.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.SnapshotEvent;
import de.bennyboer.author.eventsourcing.sample.SampleAggregate;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class SnapshottedEvent implements Event, SnapshotEvent {

    private static final EventName NAME = EventName.of("SNAPSHOTTED");

    private static final Version VERSION = Version.zero();

    String title;

    String description;

    Instant deletedAt;

    private SnapshottedEvent(String title, String description, Instant deletedAt) {
        this.title = title;
        this.description = description;
        this.deletedAt = deletedAt;
    }

    public static SnapshottedEvent of(SampleAggregate aggregate) {
        return new SnapshottedEvent(aggregate.getTitle(), aggregate.getDescription(), aggregate.getDeletedAt());
    }

    @Override
    @JsonIgnore
    public EventName getEventName() {
        return NAME;
    }

    @Override
    @JsonIgnore
    public Version getVersion() {
        return VERSION;
    }

}
