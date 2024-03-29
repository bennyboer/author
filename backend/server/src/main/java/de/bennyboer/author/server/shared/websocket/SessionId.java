package de.bennyboer.author.server.shared.websocket;

import io.javalin.websocket.WsContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionId {

    String value;

    public static SessionId of(WsContext ctx) {
        checkNotNull(ctx, "Context must be given");

        return new SessionId(ctx.sessionId());
    }

    @Override
    public String toString() {
        return String.format("SessionId(%s)", value);
    }

}
