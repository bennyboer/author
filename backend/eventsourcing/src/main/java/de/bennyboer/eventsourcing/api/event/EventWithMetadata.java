package de.bennyboer.eventsourcing.api.event;

import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventWithMetadata {

    Event event;

    EventMetadata metadata;

    public static EventWithMetadata of(Event event, EventMetadata metadata) {
        checkNotNull(event, "Event must not be null");
        checkNotNull(metadata, "EventMetadata must not be null");

        return new EventWithMetadata(event, metadata);
    }

}
