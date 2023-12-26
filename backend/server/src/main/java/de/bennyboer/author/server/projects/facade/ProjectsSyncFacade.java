package de.bennyboer.author.server.projects.facade;

import de.bennyboer.author.project.ProjectId;
import de.bennyboer.author.project.ProjectsService;
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
                .flatMap(lookupRepo::update);
    }
    
    public Mono<Void> updateInLookup(ProjectId projectId) {
        return projectsService.get(projectId)
                .flatMap(lookupRepo::update);
    }

    public Mono<Void> removeFromLookup(ProjectId projectId) {
        return lookupRepo.remove(projectId);
    }

}
