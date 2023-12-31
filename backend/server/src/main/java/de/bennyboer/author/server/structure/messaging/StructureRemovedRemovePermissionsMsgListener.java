package de.bennyboer.author.server.structure.messaging;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.structure.facade.StructurePermissionsFacade;
import de.bennyboer.author.structure.Structure;
import de.bennyboer.author.structure.StructureEvent;
import de.bennyboer.author.structure.StructureId;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class StructureRemovedRemovePermissionsMsgListener implements AggregateEventMessageListener {

    private final StructurePermissionsFacade permissionsFacade;

    @Override
    public AggregateType aggregateType() {
        return Structure.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(StructureEvent.REMOVED.getName());
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        StructureId structureId = StructureId.of(message.getAggregateId());

        return permissionsFacade.removePermissionsForStructure(structureId);
    }

}
