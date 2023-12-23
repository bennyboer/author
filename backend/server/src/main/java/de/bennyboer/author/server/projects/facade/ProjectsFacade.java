package de.bennyboer.author.server.projects.facade;

import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.project.ProjectsService;
import de.bennyboer.author.server.projects.api.ProjectDTO;
import de.bennyboer.author.server.projects.transformer.ProjectTransformer;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class ProjectsFacade {

    ProjectsService projectsService;

    public Mono<ProjectDTO> getProject(String id) {
        ProjectId treeId = ProjectId.of(id);

        return projectsService.get(treeId)
                .map(ProjectTransformer::toApi);
    }

    public Mono<Void> create(String name, Agent agent) {
        return projectsService.create(ProjectName.of(name), agent).then();
    }

    public Mono<Void> rename(String id, long version, String name, Agent agent) {
        return projectsService.rename(ProjectId.of(id), Version.of(version), ProjectName.of(name), agent).then();
    }

    public Mono<Void> remove(String id, long version, Agent agent) {
        return projectsService.remove(ProjectId.of(id), Version.of(version), agent).then();
    }

}
