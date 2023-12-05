package de.bennyboer.author.server.websocket;

import de.bennyboer.author.server.structure.transformer.TreeEventTransformer;
import de.bennyboer.author.server.websocket.subscriptions.EventTopic;
import de.bennyboer.author.structure.tree.api.Tree;
import de.bennyboer.eventsourcing.api.EventPublisher;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventWithMetadata;
import de.bennyboer.eventsourcing.api.event.metadata.EventMetadata;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import java.util.Map;

@Value
@AllArgsConstructor
public class WebSocketEventPublisher implements EventPublisher {

    WebSocketService webSocketService;

    @Override
    public Mono<Void> publish(EventWithMetadata event) {
        var metadata = event.getMetadata();
        var topic = EventTopic.of(
                metadata.getAggregateType(),
                metadata.getAggregateId(),
                metadata.getAggregateVersion()
        );
        var eventName = event.getEvent().getName();
        var eventVersion = event.getEvent().getVersion();
        var payload = mapEventToPayload(event.getEvent(), metadata);

        return Mono.fromRunnable(() -> webSocketService.publishEvent(topic, eventName, eventVersion, payload));
    }

    private Map<String, Object> mapEventToPayload(Event event, EventMetadata metadata) {
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
