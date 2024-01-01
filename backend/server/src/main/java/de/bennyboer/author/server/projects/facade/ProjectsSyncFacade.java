package de.bennyboer.author.server.projects.facade;

import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectsService;
import de.bennyboer.author.server.projects.persistence.lookup.LookupProject;
import de.bennyboer.author.server.projects.persistence.lookup.ProjectLookupRepo;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@AllArgsConstructor
public class ProjectsSyncFacade {

    ProjectsService projectsService;

    ProjectLookupRepo lookupRepo;

    public Mono<Void> addToLookup(ProjectId projectId) {
        return projectsService.get(projectId)
                .map(this::toLookupProject)
                .flatMap(lookupRepo::update);
    }

    public Mono<Void> updateInLookup(ProjectId projectId) {
        return projectsService.get(projectId)
                .map(this::toLookupProject)
                .flatMap(lookupRepo::update);
    }

    public Mono<Void> removeFromLookup(ProjectId projectId) {
        return lookupRepo.remove(projectId);
    }

    private LookupProject toLookupProject(Project project) {
        return LookupProject.of(
                project.getId(),
                project.getVersion(),
                project.getName(),
                project.getCreatedAt()
        );
    }

}
