package de.bennyboer.author.server.websocket.messages;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class EventMessage {

    // TODO Resource that the event is about (Aggregate Type/name, Aggregate ID, Aggregate Version)
    // TODO Event type/name and version (to support old event versions)
    // TODO Event payload

    public static EventMessage of() {
        return new EventMessage();
    }

}
