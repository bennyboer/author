package de.bennyboer.eventsourcing.api.event;

import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventWithMetadata {

    Event event;

    EventMetadata metadata;

    public static EventWithMetadata of(Event event, EventMetadata metadata) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("EventMetadata must not be null");
        }

        return new EventWithMetadata(event, metadata);
    }

}
