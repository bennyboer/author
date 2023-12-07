package de.bennyboer.author.server.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;

/**
 * Since the server is currently not expected to face any severe load
 * we will rely on an embedded message broker.
 * Once the server is facing load we should be easily able to switch to
 * an external message broker cluster.
 */
@Slf4j
public class EmbeddedMessageBroker {

    private final EmbeddedActiveMQ server;

    public EmbeddedMessageBroker() {
        server = new EmbeddedActiveMQ();

        try {
            configure();
        } catch (Exception e) {
            log.error("Failed to configure embedded message broker", e);
            throw new RuntimeException(e);
        }
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            log.error("Failed to start embedded message broker", e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            log.error("Failed to stop embedded message broker", e);
            throw new RuntimeException(e);
        }
    }

    private void configure() throws Exception {
        Configuration config = new ConfigurationImpl();

        config.addAcceptorConfiguration("in-vm", "vm://0");
        config.addAcceptorConfiguration("tcp", "tcp://127.0.0.1:61616");

        server.setConfiguration(config);
    }

}
