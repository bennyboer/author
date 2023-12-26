package de.bennyboer.author.server.shared.messaging.events;

import de.bennyboer.author.eventsourcing.event.Event;

import java.util.Map;

public interface AggregateEventPayloadTransformer {

    Map<String, Object> toApi(Event event);

}
