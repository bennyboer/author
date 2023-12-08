package de.bennyboer.author.server.messaging;

import de.bennyboer.author.structure.tree.api.Tree;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSContext;
import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class MessagingConfig {

    private static Map<AggregateType, Topic> TOPICS_BY_AGGREGATE_TYPE = new HashMap<>();

    private static EmbeddedMessageBroker broker;

    public static void startBroker() {
        if (Optional.ofNullable(broker).isPresent()) {
            log.warn("Embedded message broker already started");
            return;
        }

        log.info("Starting embedded message broker");
        broker = new EmbeddedMessageBroker();
        broker.start();
        setupResources();
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

    public static JMSContext getContext() {
        return broker.getContext();
    }

    public static Topic getTopic(AggregateType type) {
        return TOPICS_BY_AGGREGATE_TYPE.get(type);
    }

    private static void setupResources() {
        JMSContext ctx = getContext();

        createTopics(ctx);
    }

    private static void createTopics(JMSContext ctx) {
        /*
        When calling createTopic() the topic is created on the broker when using ActiveMQ Artemis.
        See https://activemq.apache.org/how-do-i-create-new-destinations.html
         */
        Topic tree = ctx.createTopic("tree");
        TOPICS_BY_AGGREGATE_TYPE.put(Tree.TYPE, tree);
    }

}
