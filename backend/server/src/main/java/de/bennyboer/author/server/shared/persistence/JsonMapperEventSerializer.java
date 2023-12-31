package de.bennyboer.author.server.shared.persistence;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.serialization.EventSerializer;
import io.javalin.json.JsonMapper;
import lombok.Value;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Map;
import java.util.function.Function;

@Value
public class JsonMapperEventSerializer implements EventSerializer {

    JsonMapper jsonMapper;

    Function<Event, Map<String, Object>> serializer;

    TriFunction<Map<String, Object>, EventName, Version, Event> deserializer;

    @Override
    public String serialize(Event event) {
        Map<String, Object> payload = serializer.apply(event);

        return jsonMapper.toJsonString(payload, Map.class);
    }

    @Override
    public Event deserialize(String event, EventName eventName, Version eventVersion) {
        Map<String, Object> payload = jsonMapper.fromJsonString(event, Map.class);

        return deserializer.apply(payload, eventName, eventVersion);
    }

}
