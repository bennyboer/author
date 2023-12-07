package de.bennyboer.author.server.messaging;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class MessagingConfig {

    private static EmbeddedMessageBroker broker;

    public static void startBroker() {
        if (Optional.ofNullable(broker).isPresent()) {
            log.warn("Embedded message broker already started");
            return;
        }

        log.info("Starting embedded message broker");
        broker = new EmbeddedMessageBroker();
        broker.start();
    }

    public static void stopBroker() {
        if (Optional.ofNullable(broker).isEmpty()) {
            log.warn("Embedded message broker already stopped");
            return;
        }

        log.info("Stopping embedded message broker");
        broker.stop();
        broker = null;
    }

}
