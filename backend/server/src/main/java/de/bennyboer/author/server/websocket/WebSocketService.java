package de.bennyboer.author.server.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.author.server.websocket.messages.WebSocketMessage;
import io.javalin.websocket.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketService {

    private final Map<SessionId, WsContext> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = configureObjectMapper(new ObjectMapper());

    public static WebSocketService getInstance() {
        return InstanceHolder.INSTANCE;
    }

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

        tryDeserializeMessage(ctx).ifPresent(msg -> {
            onMessage(ctx, msg);
        });

        // TODO Deal with heartbeat message from client (the client is responsible for sending the heartbeat messages)

        log.debug(
                "Received message '{}' from session ID '{}'",
                ctx.message(),
                sessionId.getValue()
        );
    }

    private void onMessage(WsContext ctx, WebSocketMessage msg) {
        switch (msg.getMethod()) {
            case HEARTBEAT -> sendHeartbeatResponse(ctx);
            default -> {
                // TODO Handle message, for now we just send it back
                trySerializeMessage(msg).ifPresent(ctx::send);
            }
        }
    }

    private void sendHeartbeatResponse(WsContext ctx) {
        trySerializeMessage(WebSocketMessage.heartbeat()).ifPresent(ctx::send);
    }

    private Optional<WebSocketMessage> tryDeserializeMessage(WsMessageContext ctx) {
        try {
            var msg = objectMapper.readValue(ctx.message(), WebSocketMessage.class);
            if (!msg.isValid()) {
                log.warn("Received invalid WebSocket message: {}", ctx.message());
                return Optional.empty();
            }

            return Optional.of(msg);
        } catch (Exception e) {
            log.error("Error while reading WebSocket message", e);
            return Optional.empty();
        }
    }

    private Optional<String> trySerializeMessage(WebSocketMessage message) {
        try {
            return Optional.of(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("Error while writing WebSocket message", e);
            return Optional.empty();
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

    private static final class InstanceHolder {

        private static final WebSocketService INSTANCE = new WebSocketService();

    }

}
