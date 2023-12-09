package de.bennyboer.author.server.shared.messaging;

import de.bennyboer.author.server.shared.messaging.messages.AggregateEventMessage;
import de.bennyboer.author.server.structure.transformer.TreeEventTransformer;
import de.bennyboer.author.structure.tree.model.Tree;
import de.bennyboer.eventsourcing.api.EventPublisher;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventWithMetadata;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import io.javalin.json.JsonMapper;
import lombok.Value;
import reactor.core.publisher.Mono;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Value
public class MessagingEventPublisher implements EventPublisher {

    Messaging messaging;

    JsonMapper jsonMapper;

    JMSContext ctx;

    JMSProducer producer;

    Map<AggregateType, AggregateEventPayloadTransformer> topicsByAggregateType = new HashMap<>();

    public MessagingEventPublisher(Messaging messaging, JsonMapper jsonMapper) {
        this.messaging = messaging;
        this.jsonMapper = jsonMapper;

        ctx = messaging.getContext();
        producer = ctx.createProducer();
    }

    @Override
    public Mono<Void> publish(EventWithMetadata event) {
        Destination destination = getDestination(event);

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

    public void registerAggregateEventPayloadTransformer(
            AggregateType type,
            AggregateEventPayloadTransformer transformer
    ) {
        topicsByAggregateType.put(type, transformer);
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

    private Destination getDestination(EventWithMetadata event) {
        return messaging.getTopic(event.getMetadata().getAggregateType());
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