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
public class UnsubscribeMessage {

    // TODO Resource to unsubscribe from (Aggregate Type/name, Aggregate ID)

    public static UnsubscribeMessage of() {
        return new UnsubscribeMessage();
    }

}
