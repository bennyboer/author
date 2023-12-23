package de.bennyboer.author.server.projects.facade;

import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectsService;
import de.bennyboer.author.server.projects.api.ProjectDTO;
import de.bennyboer.author.server.projects.permissions.ProjectPermissionsService;
import de.bennyboer.author.server.projects.transformer.ProjectTransformer;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.projects.permissions.ProjectAction.READ;

@Value
@AllArgsConstructor
public class ProjectsQueryFacade {

    ProjectsService projectsService;

    ProjectPermissionsService permissionsService;

    public Mono<ProjectDTO> getProject(String id, Agent agent) {
        ProjectId projectId = ProjectId.of(id);

        return permissionsService.assertHasPermission(agent, READ, projectId)
                .then(projectsService.get(projectId))
                .map(ProjectTransformer::toApi);
    }

}
