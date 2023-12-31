package de.bennyboer.author.server.structure.messaging;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectEvent;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import de.bennyboer.author.server.structure.facade.StructureSyncFacade;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class ProjectCreatedCreateStructureMsgListener implements AggregateEventMessageListener {

    private final StructureSyncFacade syncFacade;

    @Override
    public AggregateType aggregateType() {
        return Project.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(ProjectEvent.CREATED.getName());
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        String projectId = message.getAggregateId();

        return message.getUserId()
                .map(UserId::of)
                .map(userId -> syncFacade.create(projectId, userId))
                .orElse(Mono.empty());
    }

}
