package de.bennyboer.author.server.project.facade;

import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.project.ProjectService;
import de.bennyboer.author.server.project.api.ProjectDTO;
import de.bennyboer.author.server.project.transformer.ProjectTransformer;
import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class ProjectFacade {

    ProjectService projectService;

    public Mono<ProjectDTO> getProject(String id) {
        ProjectId treeId = ProjectId.of(id);

        return projectService.get(treeId)
                .map(ProjectTransformer::toApi);
    }

    public Mono<Void> create(String name, Agent agent) {
        return projectService.create(ProjectName.of(name), agent).then();
    }

    public Mono<Void> rename(String id, long version, String name, Agent agent) {
        return projectService.rename(ProjectId.of(id), Version.of(version), ProjectName.of(name), agent).then();
    }

    public Mono<Void> remove(String id, long version, Agent agent) {
        return projectService.remove(ProjectId.of(id), Version.of(version), agent).then();
    }

}
