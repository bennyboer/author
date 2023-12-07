package de.bennyboer.author.server.messaging;

import io.javalin.Javalin;
import io.javalin.plugin.Plugin;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class MessagingPlugin implements Plugin {

    @Override
    public void apply(@NotNull Javalin app) {
        MessagingConfig.startBroker();
        app.events(event -> event.serverStopping(MessagingConfig::stopBroker));
    }

}
