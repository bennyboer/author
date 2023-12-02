package de.bennyboer.author.server.websocket;

import io.javalin.websocket.WsContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionId {

    String value;

    public static SessionId of(WsContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("ctx must not be null");
        }

        return new SessionId(ctx.getSessionId());
    }

    @Override
    public String toString() {
        return String.format("SessionId(%s)", value);
    }

}
