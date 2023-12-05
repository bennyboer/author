package de.bennyboer.author.server.websocket.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class HeartbeatMessage {

    public static HeartbeatMessage of() {
        return new HeartbeatMessage();
    }

}
