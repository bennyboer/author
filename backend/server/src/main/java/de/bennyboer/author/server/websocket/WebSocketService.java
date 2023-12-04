package de.bennyboer.author.server.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.author.server.websocket.messages.WebSocketMessage;
import io.javalin.websocket.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@AllArgsConstructor
public class WebSocketService {

    private final Map<SessionId, WsContext> sessions = new ConcurrentHashMap<>();

    public void onConnect(WsConnectContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        sessions.put(sessionId, ctx);
        log.debug("User connected via WebSocket with session ID '{}'", sessionId.getValue());
    }

    public void onClose(WsCloseContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        closeSessionIfOpen(sessionId);

        log.debug(
                "Closed WebSocket for session ID '{}' with status code '{}' and reason '{}'",
                sessionId.getValue(),
                ctx.status(),
                ctx.reason()
        );
    }

    public void onError(WsErrorContext ctx) {
        SessionId sessionId = SessionId.of(ctx);
        closeSessionIfOpen(sessionId);

        log.error(
                "Error in WebSocket for session ID '{}'",
                sessionId.getValue(),
                ctx.error()
        );
    }

    public void onMessage(WsMessageContext ctx) {
        SessionId sessionId = SessionId.of(ctx);

        log.debug(
                "Received message '{}' from session ID '{}'",
                ctx.message(),
                sessionId.getValue()
        );

        WebSocketMessage msg = ctx.messageAsClass(WebSocketMessage.class);
        onMessage(ctx, msg);
    }

    private void onMessage(WsContext ctx, WebSocketMessage msg) {
        switch (msg.getMethod()) {
            case HEARTBEAT -> ctx.send(WebSocketMessage.heartbeat());
            default -> throw new IllegalArgumentException(
                    "Encountered message with unsupported method from client" + msg.getMethod()
            );
        }
    }

    private void closeSessionIfOpen(SessionId sessionId) {
        Optional.ofNullable(sessions.remove(sessionId))
                .filter(ctx -> ctx.session.isOpen())
                .ifPresent(ctx -> ctx.session.close());
    }

    private static ObjectMapper configureObjectMapper(ObjectMapper mapper) {
        mapper.registerModule(new Jdk8Module());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

}
