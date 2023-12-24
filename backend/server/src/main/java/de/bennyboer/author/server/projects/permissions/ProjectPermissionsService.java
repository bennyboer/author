package de.bennyboer.author.server.projects.permissions;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.eventsourcing.aggregate.AggregateType;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.permissions.*;
import de.bennyboer.author.permissions.repo.PermissionsRepo;
import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.server.shared.permissions.AggregatePermissionsService;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.author.server.projects.permissions.ProjectAction.READ;

public class ProjectPermissionsService extends AggregatePermissionsService<ProjectId, ProjectAction> {

    public ProjectPermissionsService(PermissionsRepo permissionsRepo, PermissionsEventPublisher eventPublisher) {
        super(permissionsRepo, eventPublisher);
    }

    @Override
    public AggregateType getAggregateType() {
        return Project.TYPE;
    }

    @Override
    public ResourceId getResourceId(ProjectId id) {
        return ResourceId.of(id.getValue());
    }

    @Override
    public Action toAction(ProjectAction action) {
        return Action.of(action.name());
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, ProjectId projectId) {
        Resource resource = toResource(projectId);

        Set<Permission> permissions = Arrays.stream(ProjectAction.values())
                .map(action -> Permission.builder()
                        .user(userId)
                        .isAllowedTo(toAction(action))
                        .on(resource))
                .collect(Collectors.toSet());

        return addPermissions(permissions);
    }

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return removePermissionsByUserId(userId);
    }

    public Mono<Void> removePermissionsOnProject(ProjectId projectId) {
        return removePermissionsByResource(projectId);
    }

    public Mono<Boolean> hasPermissionToReceiveEvents(Agent agent, ProjectId projectId) {
        return hasPermission(agent, READ, projectId);
    }
    
}
