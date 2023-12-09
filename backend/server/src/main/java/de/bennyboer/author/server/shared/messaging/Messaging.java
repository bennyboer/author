package de.bennyboer.author.server.shared.messaging;

import de.bennyboer.author.structure.tree.model.Tree;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.JMSContext;
import javax.jms.Topic;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Since the server is currently not expected to face any severe load
 * we will rely on an embedded message broker.
 * Once the server is facing load we should be easily able to switch to
 * an external message broker cluster.
 */
@Slf4j
public class Messaging {

    private static final String BROKER_URI = "vm://0";

    private final EmbeddedActiveMQ server;

    private ActiveMQConnectionFactory connectionFactory;

    private JMSContext ctx;

    private final Map<AggregateType, Topic> topicsByAggregateType = new HashMap<>();

    public Messaging() {
        server = new EmbeddedActiveMQ();

        try {
            configure();
            start();
        } catch (Exception e) {
            log.error("Failed to configure and start embedded message broker", e);
            throw new RuntimeException(e);
        }
    }

    public Topic getTopic(AggregateType type) {
        return topicsByAggregateType.get(type);
    }

    public void stop() {
        try {
            server.stop();
            connectionFactory.close();
        } catch (Exception e) {
            log.error("Failed to stop embedded message broker", e);
            throw new RuntimeException(e);
        }
    }

    public JMSContext getContext() {
        return ctx;
    }

    public void registerAggregateType(AggregateType type) {
        createTopic(type);
    }

    private void start() throws Exception {
        server.start();
        connectionFactory = new ActiveMQConnectionFactory(BROKER_URI);
        ctx = connectionFactory.createContext();
    }

    private void configure() throws Exception {
        Configuration config = new ConfigurationImpl();

        config.setSecurityEnabled(false);
        config.addAcceptorConfiguration("in-vm", BROKER_URI);

        server.setConfiguration(config);
    }

    private void createTopic(AggregateType aggregateType) {
        /*
        When calling createTopic() the topic is created on the broker when using ActiveMQ Artemis.
        See https://activemq.apache.org/how-do-i-create-new-destinations.html
         */
        Topic tree = ctx.createTopic(aggregateType.getValue().toLowerCase(Locale.ROOT));
        topicsByAggregateType.put(Tree.TYPE, tree);
    }

}
