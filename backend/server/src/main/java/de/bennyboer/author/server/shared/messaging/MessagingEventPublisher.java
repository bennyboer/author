package de.bennyboer.author.server.shared.messaging;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.author.server.shared.messaging.messages.AggregateEventMessage;
import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.event.EventWithMetadata;
import io.javalin.json.JsonMapper;
import lombok.Value;
import reactor.core.publisher.Mono;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Value
public class MessagingEventPublisher implements EventPublisher {

    Messaging messaging;

    JsonMapper jsonMapper;

    JMSContext ctx;

    JMSProducer producer;

    Map<AggregateType, AggregateEventPayloadTransformer> eventPayloadTransformerByAggregateType = new HashMap<>();

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
            textMessage.setStringProperty("eventName", event.getEvent().getEventName().getValue());
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
        eventPayloadTransformerByAggregateType.put(type, transformer);
    }

    private AggregateEventMessage createMessage(EventWithMetadata event) {
        Map<String, Object> payload = mapEventToPayload(event);

        return AggregateEventMessage.of(
                event.getMetadata().getAggregateType().getValue(),
                event.getMetadata().getAggregateId().getValue(),
                event.getMetadata().getAggregateVersion().getValue(),
                event.getMetadata().getDate(),
                event.getEvent().getEventName().getValue(),
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
        var transformer = Optional.ofNullable(eventPayloadTransformerByAggregateType.get(aggregateType))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No event transformer found for aggregate type '" + aggregateType + "'"
                ));

        return transformer.toApi(event);
    }

}
