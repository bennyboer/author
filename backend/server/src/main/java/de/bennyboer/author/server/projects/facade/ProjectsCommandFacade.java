package de.bennyboer.author.server.projects.facade;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.project.ProjectsService;
import de.bennyboer.author.server.projects.permissions.ProjectPermissionsService;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.projects.permissions.ProjectAction.*;

@Value
@AllArgsConstructor
public class ProjectsCommandFacade {

    ProjectsService projectsService;

    ProjectPermissionsService permissionsService;

    public Mono<Void> create(String name, Agent agent) {
        return permissionsService.assertHasPermission(agent, CREATE)
                .then(projectsService.create(ProjectName.of(name), agent))
                .then();
    }

    public Mono<Void> rename(String id, long version, String name, Agent agent) {
        ProjectId projectId = ProjectId.of(id);

        return permissionsService.assertHasPermission(agent, RENAME, projectId)
                .then(projectsService.rename(projectId, Version.of(version), ProjectName.of(name), agent))
                .then();
    }

    public Mono<Void> remove(String id, long version, Agent agent) {
        ProjectId projectId = ProjectId.of(id);

        return permissionsService.assertHasPermission(agent, REMOVE, projectId)
                .then(projectsService.remove(projectId, Version.of(version), agent))
                .then();
    }

}
