package de.bennyboer.author.server.structure.messaging;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.structure.facade.TreePermissionsFacade;
import de.bennyboer.author.structure.tree.Tree;
import de.bennyboer.author.structure.tree.TreeId;
import de.bennyboer.author.structure.tree.create.CreatedEvent;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class TreeCreatedAddPermissionsMsgListener implements AggregateEventMessageListener {

    private final TreePermissionsFacade permissionsFacade;

    @Override
    public AggregateType aggregateType() {
        return Tree.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(CreatedEvent.NAME);
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        TreeId treeId = TreeId.of(message.getAggregateId());

        return message.getUserId()
                .map(UserId::of)
                .map(userId -> permissionsFacade.addPermissionsForCreator(userId, treeId))
                .orElse(Mono.empty());
    }

}
