package de.bennyboer.author.server.shared.websocket.api;

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
    String token;

    @Nullable
    HeartbeatMessage heartbeat;

    @Nullable
    EventMessage event;

    @Nullable
    PermissionEventMessage permissionEvent;

    @Nullable
    SubscribeMessage subscribe;

    @Nullable
    SubscribeToPermissionsMessage subscribeToPermissions;

    @Nullable
    UnsubscribeMessage unsubscribe;

    @Nullable
    UnsubscribeFromPermissionsMessage unsubscribeFromPermissions;

    public static WebSocketMessage heartbeat() {
        return WebSocketMessage.builder()
                .method(WebSocketMessageMethod.HEARTBEAT)
                .heartbeat(HeartbeatMessage.of())
                .build();
    }

    public static WebSocketMessage event(EventMessage event) {
        return WebSocketMessage.builder()
                .method(WebSocketMessageMethod.EVENT)
                .event(event)
                .build();
    }

    public static WebSocketMessage permissionEvent(PermissionEventMessage permissionEvent) {
        return WebSocketMessage.builder()
                .method(WebSocketMessageMethod.PERMISSION_EVENT)
                .permissionEvent(permissionEvent)
                .build();
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    public Optional<HeartbeatMessage> getHeartbeat() {
        return Optional.ofNullable(heartbeat);
    }

    public Optional<EventMessage> getEvent() {
        return Optional.ofNullable(event);
    }

    public Optional<PermissionEventMessage> getPermissionEvent() {
        return Optional.ofNullable(permissionEvent);
    }

    public Optional<SubscribeMessage> getSubscribe() {
        return Optional.ofNullable(subscribe);
    }

    public Optional<SubscribeToPermissionsMessage> getSubscribeToPermissions() {
        return Optional.ofNullable(subscribeToPermissions);
    }

    public Optional<UnsubscribeMessage> getUnsubscribe() {
        return Optional.ofNullable(unsubscribe);
    }

    public Optional<UnsubscribeFromPermissionsMessage> getUnsubscribeFromPermissions() {
        return Optional.ofNullable(unsubscribeFromPermissions);
    }

}
