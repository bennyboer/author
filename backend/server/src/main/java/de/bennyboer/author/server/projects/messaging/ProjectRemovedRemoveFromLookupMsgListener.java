package de.bennyboer.author.server.projects.messaging;

import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectEvent;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.server.projects.facade.ProjectsSyncFacade;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class ProjectRemovedRemoveFromLookupMsgListener implements AggregateEventMessageListener {

    private final ProjectsSyncFacade syncFacade;

    @Override
    public AggregateType aggregateType() {
        return Project.TYPE;
    }

    @Override
    public Optional<EventName> eventName() {
        return Optional.of(ProjectEvent.REMOVED.getName());
    }

    @Override
    public Mono<Void> onMessage(AggregateEventMessage message) {
        ProjectId projectId = ProjectId.of(message.getAggregateId());

        return syncFacade.removeFromLookup(projectId);
    }

}
