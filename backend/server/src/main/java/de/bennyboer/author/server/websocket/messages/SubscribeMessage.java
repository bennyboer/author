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
public class SubscribeMessage {

    // TODO Resource to subscribe to (Aggregate Type/name, Aggregate ID)
    String test;

    public static SubscribeMessage of() {
        return new SubscribeMessage("TEST");
    }

}
