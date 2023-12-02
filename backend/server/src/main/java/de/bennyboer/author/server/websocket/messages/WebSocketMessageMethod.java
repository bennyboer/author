package de.bennyboer.author.server.websocket.messages;

public enum WebSocketMessageMethod {
    HEARTBEAT,
    SUBSCRIBE,
    UNSUBSCRIBE,
    DISPATCH_COMMAND,
    EVENT
}
