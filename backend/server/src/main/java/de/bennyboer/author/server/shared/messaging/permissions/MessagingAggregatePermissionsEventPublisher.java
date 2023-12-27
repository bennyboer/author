package de.bennyboer.author.server.shared.messaging.permissions;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.permissions.PermissionsEventPublisher;
import de.bennyboer.author.permissions.ResourceId;
import de.bennyboer.author.permissions.event.PermissionEvent;
import de.bennyboer.author.server.shared.messaging.Messaging;
import io.javalin.json.JsonMapper;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.jms.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class MessagingAggregatePermissionsEventPublisher implements PermissionsEventPublisher {

    Messaging messaging;

    JsonMapper jsonMapper;

    JMSContext ctx;

    JMSProducer producer;

    public MessagingAggregatePermissionsEventPublisher(
            Messaging messaging,
            JsonMapper jsonMapper
    ) {
        this.messaging = messaging;
        this.jsonMapper = jsonMapper;

        ctx = messaging.getContext();
        producer = ctx.createProducer();
    }

    @Override
    public Mono<Void> publish(PermissionEvent event) {
        var messages = createMessages(event);
        var messagesByDestination = groupMessagesByDestination(messages);

        return Flux.fromIterable(messagesByDestination.entrySet())
                .flatMap(entry -> publishMessages(entry.getKey(), entry.getValue()))
                .then();
    }

    private Mono<Void> publishMessages(Destination destination, List<AggregatePermissionEventMessage> messages) {
        return Flux.fromIterable(messages)
                .flatMap(message -> publishMessage(destination, message))
                .then();
    }

    private Mono<Void> publishMessage(Destination destination, AggregatePermissionEventMessage message) {
        return Mono.fromRunnable(() -> {
            String json = jsonMapper.toJsonString(message, AggregatePermissionEventMessage.class);

            TextMessage textMessage = ctx.createTextMessage(json);

            try {
                textMessage.setStringProperty("userId", message.getUserId());
                textMessage.setStringProperty("action", message.getAction());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }

            message.getAggregateId().ifPresent(aggregateId -> {
                try {
                    textMessage.setStringProperty("aggregateId", aggregateId);
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            });

            // TODO Instead of publishing directly we should store the messages in an outbox
            producer.send(destination, textMessage);
        });
    }

    private Map<Destination, List<AggregatePermissionEventMessage>> groupMessagesByDestination(List<AggregatePermissionEventMessage> messages) {
        return messages.stream()
                .collect(Collectors.groupingBy(
                        message -> getDestination(AggregateType.of(message.getAggregateType())),
                        Collectors.toList()
                ));
    }

    private List<AggregatePermissionEventMessage> createMessages(PermissionEvent event) {
        AggregatePermissionEventType eventType = switch (event.getType()) {
            case ADDED -> AggregatePermissionEventType.ADDED;
            case REMOVED -> AggregatePermissionEventType.REMOVED;
        };

        return event.getPermissions()
                .stream()
                .map(permission -> AggregatePermissionEventMessage.of(
                        eventType,
                        permission.getUserId().getValue(),
                        permission.getResource().getType().getName(),
                        permission.getResource().getId().map(ResourceId::getValue).orElse(null),
                        permission.getAction().getName()
                ))
                .toList();
    }

    private Destination getDestination(AggregateType aggregateType) {
        return messaging.getTopic(aggregateType);
    }

}
