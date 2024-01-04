package de.bennyboer.author.server.shared.modules;

import de.bennyboer.author.server.AppConfig;
import de.bennyboer.author.server.shared.messaging.Messaging;
import de.bennyboer.author.server.shared.websocket.WebSocketService;
import io.javalin.json.JsonMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModuleConfig {

    Messaging messaging;

    JsonMapper jsonMapper;

    WebSocketService webSocketService;

    AppConfig appConfig;

    public static ModuleConfig of(
            Messaging messaging,
            JsonMapper jsonMapper,
            WebSocketService webSocketService,
            AppConfig appConfig
    ) {
        checkNotNull(messaging, "Messaging must be given");
        checkNotNull(jsonMapper, "JsonMapper must be given");
        checkNotNull(webSocketService, "WebSocketService must be given");
        checkNotNull(appConfig, "App config must be given");

        return new ModuleConfig(messaging, jsonMapper, webSocketService, appConfig);
    }

}
