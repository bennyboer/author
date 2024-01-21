package de.bennyboer.author.server.assets.transformer;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;

import java.util.Map;

public class AssetEventTransformer {

    public static Map<String, Object> toApi(Event event) {
        return switch (event) {
            default -> Map.of();
        };
    }

    public static Map<String, Object> toSerialized(Event event) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
        //        return switch (event) {
        //            // TODO
        //            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName
        //            ());
        //        };
    }

    public static Event fromSerialized(Map<String, Object> serialized, EventName eventName, Version ignoredVersion) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
        //        AssetEvent event = AssetEvent.fromName(eventName);
        //
        //        return switch (event) {
        //        };
    }

}
