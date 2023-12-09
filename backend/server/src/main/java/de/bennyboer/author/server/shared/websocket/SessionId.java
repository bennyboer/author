package de.bennyboer.author.server.shared.websocket;

import io.javalin.websocket.WsContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionId {

    String value;

    public static SessionId of(WsContext ctx) {
        checkNotNull(ctx, "Context must not be null");

        return new SessionId(ctx.getSessionId());
    }

    @Override
    public String toString() {
        return String.format("SessionId(%s)", value);
    }

}
