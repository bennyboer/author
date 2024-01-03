package de.bennyboer.author.server.shared.modules;

import de.bennyboer.author.server.shared.http.HttpApi;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.websocket.WebSocketService;
import io.javalin.json.JsonMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.net.URL;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModuleConfig {

    URL host;

    Messaging messaging;

    JsonMapper jsonMapper;

    HttpApi httpApi;

    WebSocketService webSocketService;

    public static ModuleConfig of(
            URL host,
            Messaging messaging,
            JsonMapper jsonMapper,
            HttpApi httpApi,
            WebSocketService webSocketService
    ) {
        checkNotNull(host, "Host must be given");
        checkNotNull(messaging, "Messaging must be given");
        checkNotNull(jsonMapper, "JsonMapper must be given");
        checkNotNull(httpApi, "AggregateApiConfig must be given");
        checkNotNull(webSocketService, "WebSocketService must be given");

        return new ModuleConfig(host, messaging, jsonMapper, httpApi, webSocketService);
    }

}
