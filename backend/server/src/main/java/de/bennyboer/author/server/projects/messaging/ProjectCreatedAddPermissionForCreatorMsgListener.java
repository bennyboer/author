package de.bennyboer.author.server.projects.messaging;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectEvent;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.server.projects.facade.ProjectsPermissionsFacade;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessage;
import de.bennyboer.author.server.shared.messaging.events.AggregateEventMessageListener;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
public class ProjectCreatedAddPermissionForCreatorMsgListener implements AggregateEventMessageListener {

    private final ProjectsPermissionsFacade permissionsFacade;

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
        ProjectId projectId = ProjectId.of(message.getAggregateId());

        return message.getUserId()
                .map(UserId::of)
                .map(userId -> permissionsFacade.addPermissionsForCreator(userId, projectId))
                .orElse(Mono.empty());
    }

}
