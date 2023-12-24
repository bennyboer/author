package de.bennyboer.author.server.projects.facade;

import de.bennyboer.author.common.UserId;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.server.projects.permissions.ProjectPermissionsService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class ProjectsPermissionsFacade {

    ProjectPermissionsService permissionsService;

    public Mono<Void> removePermissionsForUser(UserId userId) {
        return permissionsService.removePermissionsForUser(userId);
    }

    public Mono<Void> addPermissionsForCreator(UserId userId, ProjectId projectId) {
        return permissionsService.addPermissionsForCreator(userId, projectId);
    }

    public Mono<Void> removePermissionsOnProject(ProjectId projectId) {
        return permissionsService.removePermissionsOnProject(projectId);
    }

}
