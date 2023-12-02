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
    SubscribeMessage subscribe;

    @Nullable
    UnsubscribeMessage unsubscribe;

    @Nullable
    DispatchCommandMessage dispatchCommand;

    @Nullable
    EventMessage event;

    public static WebSocketMessage heartbeat(HeartbeatMessage heartbeat) {
        return new WebSocketMessage(
                WebSocketMessageMethod.HEARTBEAT,
                heartbeat,
                null,
                null,
                null,
                null
        );
    }

    public static WebSocketMessage subscribe(SubscribeMessage subscribe) {
        return new WebSocketMessage(
                WebSocketMessageMethod.SUBSCRIBE,
                null,
                subscribe,
                null,
                null,
                null
        );
    }

    public static WebSocketMessage unsubscribe(UnsubscribeMessage unsubscribe) {
        return new WebSocketMessage(
                WebSocketMessageMethod.UNSUBSCRIBE,
                null,
                null,
                unsubscribe,
                null,
                null
        );
    }

    public static WebSocketMessage dispatchCommand(DispatchCommandMessage dispatchCommand) {
        return new WebSocketMessage(
                WebSocketMessageMethod.DISPATCH_COMMAND,
                null,
                null,
                null,
                dispatchCommand,
                null
        );
    }

    public static WebSocketMessage event(EventMessage event) {
        return new WebSocketMessage(
                WebSocketMessageMethod.EVENT,
                null,
                null,
                null,
                null,
                event
        );
    }

    @JsonIgnore
    public boolean isValid() {
        return switch (method) {
            case HEARTBEAT -> heartbeat != null;
            case SUBSCRIBE -> subscribe != null;
            case UNSUBSCRIBE -> unsubscribe != null;
            case DISPATCH_COMMAND -> dispatchCommand != null;
            case EVENT -> event != null;
        };
    }

    public Optional<HeartbeatMessage> getHeartbeat() {
        return Optional.ofNullable(heartbeat);
    }

    public Optional<SubscribeMessage> getSubscribe() {
        return Optional.ofNullable(subscribe);
    }

    public Optional<UnsubscribeMessage> getUnsubscribe() {
        return Optional.ofNullable(unsubscribe);
    }

    public Optional<DispatchCommandMessage> getDispatchCommand() {
        return Optional.ofNullable(dispatchCommand);
    }

    public Optional<EventMessage> getEvent() {
        return Optional.ofNullable(event);
    }

}
