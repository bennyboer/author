package de.bennyboer.author.server.websocket.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class WebSocketMessage {

    WebSocketMessageMethod method;

    @Nullable
    HeartbeatMessage heartbeat;

    @Nullable
    EventMessage event;

    public static WebSocketMessage heartbeat() {
        return new WebSocketMessage(
                WebSocketMessageMethod.HEARTBEAT,
                HeartbeatMessage.of(),
                null
        );
    }

    public static WebSocketMessage event(EventMessage event) {
        return new WebSocketMessage(
                WebSocketMessageMethod.EVENT,
                null,
                event
        );
    }

    @JsonIgnore
    public boolean isValid() {
        return switch (method) {
            case HEARTBEAT -> heartbeat != null;
            case EVENT -> event != null;
        };
    }

    public Optional<HeartbeatMessage> getHeartbeat() {
        return Optional.ofNullable(heartbeat);
    }

    public Optional<EventMessage> getEvent() {
        return Optional.ofNullable(event);
    }

}
