package de.bennyboer.author.server.shared.modules;

import de.bennyboer.author.server.shared.messaging.Messaging;
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

    public static ModuleConfig of(
            Messaging messaging,
            JsonMapper jsonMapper
    ) {
        checkNotNull(messaging, "Messaging must be given");
        checkNotNull(jsonMapper, "JsonMapper must be given");

        return new ModuleConfig(messaging, jsonMapper);
    }

}
