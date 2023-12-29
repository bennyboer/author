package de.bennyboer.author.eventsourcing.event;

import de.bennyboer.author.eventsourcing.event.metadata.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventWithMetadata {

    Event event;

    EventMetadata metadata;

    public static EventWithMetadata of(Event event, EventMetadata metadata) {
        checkNotNull(event, "Event must be given");
        checkNotNull(metadata, "EventMetadata must be given");

        return new EventWithMetadata(event, metadata);
    }

}
