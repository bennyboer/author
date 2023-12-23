package de.bennyboer.author.server.shared.messaging;

import de.bennyboer.author.eventsourcing.aggregate.AggregateId;
import de.bennyboer.author.server.shared.messaging.messages.AggregateEventMessage;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import io.javalin.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Since the server is currently not expected to face any severe load
 * we will rely on an embedded message broker.
 * Once the server is facing load we should be easily able to switch to
 * an external message broker cluster.
 */
@Slf4j
public class Messaging {

    private static final String TOPIC_PREFIX = "aggregate.";

    private static final String BROKER_URI = "vm://0";

    private final EmbeddedActiveMQ server;

    private ActiveMQConnectionFactory connectionFactory;

    private JMSContext ctx;

    private final JsonMapper jsonMapper;

    private final Map<AggregateType, Topic> topicsByAggregateType = new HashMap<>();

    private final Map<MessageListenerId, JMSConsumer> messageListeners = new HashMap<>();

    public Messaging(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
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
            deregisterAllAggregateEventMessageListeners();
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

    public MessageListenerId registerAggregateEventMessageListener(AggregateEventMessageListener listener) {
        AggregateType type = listener.aggregateType();

        Topic topic = getTopic(type);
        String messageSelector = buildMessageSelectorForAggregateEventMessageListener(listener);

        JMSConsumer consumer = ctx.createConsumer(topic, messageSelector);
        consumer.setMessageListener(message -> parseAggregateEventMessage(message)
                .ifPresent(msg -> listener.onMessage(msg).block()));

        MessageListenerId id = MessageListenerId.create();
        messageListeners.put(id, consumer);

        return id;
    }

    public void deregisterAggregateEventMessageListener(MessageListenerId id) {
        Optional.ofNullable(messageListeners.remove(id))
                .ifPresent(JMSConsumer::close);
    }

    private void deregisterAllAggregateEventMessageListeners() {
        messageListeners.values().forEach(JMSConsumer::close);
        messageListeners.clear();
    }

    private Optional<AggregateEventMessage> parseAggregateEventMessage(Message msg) {
        if (msg instanceof TextMessage textMessage) {
            try {
                String json = textMessage.getText();
                return Optional.of(jsonMapper.fromJsonString(json, AggregateEventMessage.class));
            } catch (Exception e) {
                log.error("Failed to parse AggregateEventMessage", e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private String buildMessageSelectorForAggregateEventMessageListener(AggregateEventMessageListener listener) {
        Optional<AggregateId> aggregateId = listener.aggregateId();
        Optional<EventName> eventName = listener.eventName();
        String messageSelector = "";
        if (aggregateId.isPresent()) {
            messageSelector += String.format("aggregateId = '%s'", aggregateId.get().getValue());
        }
        if (eventName.isPresent()) {
            if (!messageSelector.isBlank()) {
                messageSelector += " AND ";
            }
            messageSelector += String.format("eventName = '%s'", eventName.get().getValue());
        }

        return messageSelector;
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
        String topicName = TOPIC_PREFIX + aggregateType.getValue().toLowerCase(Locale.ROOT);
        Topic tree = ctx.createTopic(topicName);
        topicsByAggregateType.put(aggregateType, tree);
    }

}
