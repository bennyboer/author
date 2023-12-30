package de.bennyboer.author.eventsourcing.serialization;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;

public interface EventSerialization {

    String serialize(Event event);

    Event deserialize(String event, EventName eventName, Version eventVersion);

}
