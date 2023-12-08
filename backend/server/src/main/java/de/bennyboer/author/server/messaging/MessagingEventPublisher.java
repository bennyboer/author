package de.bennyboer.author.server.messaging;

import de.bennyboer.author.server.messaging.messages.AggregateEventMessage;
import de.bennyboer.author.server.structure.transformer.TreeEventTransformer;
import de.bennyboer.author.structure.tree.api.Tree;
import de.bennyboer.eventsourcing.api.EventPublisher;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventWithMetadata;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import io.javalin.json.JsonMapper;
import reactor.core.publisher.Mono;

import javax.jms.*;
import java.util.Map;
import java.util.Optional;

public class MessagingEventPublisher implements EventPublisher {

    private final JsonMapper jsonMapper;

    JMSProducer producer;

    public MessagingEventPublisher(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Mono<Void> publish(EventWithMetadata event) {
        JMSContext ctx = MessagingConfig.getContext();
        JMSProducer producer = getProducer();
        Destination destination = getTopic(event);

        AggregateEventMessage message = createMessage(event);
        String json = jsonMapper.toJsonString(message, AggregateEventMessage.class);

        TextMessage textMessage = ctx.createTextMessage(json);
        try {
            textMessage.setStringProperty("aggregateId", event.getMetadata().getAggregateId().getValue());
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }

        // TODO Publishing the message should be done in a transaction together with the event store
        return Mono.fromRunnable(() -> producer.send(destination, textMessage));
    }

    private AggregateEventMessage createMessage(EventWithMetadata event) {
        Map<String, Object> payload = mapEventToPayload(event);

        return AggregateEventMessage.of(
                event.getMetadata().getAggregateType().getValue(),
                event.getMetadata().getAggregateId().getValue(),
                event.getMetadata().getAggregateVersion().getValue(),
                event.getMetadata().getDate(),
                event.getEvent().getName().getValue(),
                event.getEvent().getVersion().getValue(),
                payload
        );
    }

    private Destination getTopic(EventWithMetadata event) {
        return MessagingConfig.getTopic(event.getMetadata().getAggregateType());
    }

    private JMSProducer getProducer() {
        return Optional.ofNullable(producer).orElseGet(() -> MessagingConfig.getContext().createProducer());
    }

    private Map<String, Object> mapEventToPayload(EventWithMetadata eventWithMetadata) {
        EventMetadata metadata = eventWithMetadata.getMetadata();
        Event event = eventWithMetadata.getEvent();

        var aggregateType = metadata.getAggregateType();

        // TODO We should provide a registry singleton where we can register the mapping of events for an aggregate
        //  type to payloads
        // TODO For now we just hard-code the event transformers per aggregate type

        if (aggregateType == Tree.TYPE) {
            return TreeEventTransformer.toApi(event);
        } else {
            throw new IllegalArgumentException("No event transformer found for aggregate type '" + aggregateType + "'");
        }
    }

}
