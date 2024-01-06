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
    SubscribedMessage subscribed;

    @Nullable
    SubscribeToPermissionsMessage subscribeToPermissions;

    @Nullable
    SubscribedToPermissionsMessage subscribedToPermissions;

    @Nullable
    UnsubscribeMessage unsubscribe;

    @Nullable
    UnsubscribedMessage unsubscribed;

    @Nullable
    UnsubscribeFromPermissionsMessage unsubscribeFromPermissions;

    @Nullable
    UnsubscribedFromPermissionsMessage unsubscribedFromPermissions;

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

    public static WebSocketMessage subscribed(SubscribedMessage subscribed) {
        return WebSocketMessage.builder()
                .method(WebSocketMessageMethod.SUBSCRIBED)
                .subscribed(subscribed)
                .build();
    }

    public static WebSocketMessage subscribedToPermissions(SubscribedToPermissionsMessage subscribedToPermissions) {
        return WebSocketMessage.builder()
                .method(WebSocketMessageMethod.SUBSCRIBED_TO_PERMISSIONS)
                .subscribedToPermissions(subscribedToPermissions)
                .build();
    }

    public static WebSocketMessage unsubscribed(UnsubscribedMessage unsubscribed) {
        return WebSocketMessage.builder()
                .method(WebSocketMessageMethod.UNSUBSCRIBED)
                .unsubscribed(unsubscribed)
                .build();
    }

    public static WebSocketMessage unsubscribedFromPermissions(UnsubscribedFromPermissionsMessage unsubscribedFromPermissions) {
        return WebSocketMessage.builder()
                .method(WebSocketMessageMethod.UNSUBSCRIBED_FROM_PERMISSIONS)
                .unsubscribedFromPermissions(unsubscribedFromPermissions)
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

    public Optional<SubscribedMessage> getSubscribed() {
        return Optional.ofNullable(subscribed);
    }

    public Optional<SubscribeToPermissionsMessage> getSubscribeToPermissions() {
        return Optional.ofNullable(subscribeToPermissions);
    }

    public Optional<SubscribedToPermissionsMessage> getSubscribedToPermissions() {
        return Optional.ofNullable(subscribedToPermissions);
    }

    public Optional<UnsubscribeMessage> getUnsubscribe() {
        return Optional.ofNullable(unsubscribe);
    }

    public Optional<UnsubscribedMessage> getUnsubscribed() {
        return Optional.ofNullable(unsubscribed);
    }

    public Optional<UnsubscribeFromPermissionsMessage> getUnsubscribeFromPermissions() {
        return Optional.ofNullable(unsubscribeFromPermissions);
    }

    public Optional<UnsubscribedFromPermissionsMessage> getUnsubscribedFromPermissions() {
        return Optional.ofNullable(unsubscribedFromPermissions);
    }

}
