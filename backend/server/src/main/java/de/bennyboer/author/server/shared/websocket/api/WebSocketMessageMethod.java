package de.bennyboer.author.server.shared.websocket.api;

public enum WebSocketMessageMethod {
    HEARTBEAT,
    EVENT,
    PERMISSION_EVENT,
    SUBSCRIBE,
    SUBSCRIBE_TO_PERMISSIONS,
    UNSUBSCRIBE,
    UNSUBSCRIBE_FROM_PERMISSIONS
}
