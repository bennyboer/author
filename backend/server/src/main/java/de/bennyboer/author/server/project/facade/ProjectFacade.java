package de.bennyboer.author.server.project.facade;

import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectName;
import de.bennyboer.author.project.ProjectService;
import de.bennyboer.author.server.project.api.ProjectDTO;
import de.bennyboer.author.server.project.transformer.ProjectTransformer;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.Version;
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

    public Mono<Void> create(String name, UserId userId) {
        return projectService.create(ProjectName.of(name), userId).then();
    }

    public Mono<Void> rename(String id, long version, String name, UserId userId) {
        return projectService.rename(ProjectId.of(id), Version.of(version), ProjectName.of(name), userId).then();
    }

    public Mono<Void> remove(String id, long version, UserId userId) {
        return projectService.remove(ProjectId.of(id), Version.of(version), userId).then();
    }

}
