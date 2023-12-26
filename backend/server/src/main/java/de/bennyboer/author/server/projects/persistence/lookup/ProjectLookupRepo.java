package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.project.Project;
import de.bennyboer.author.project.ProjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ProjectLookupRepo {

    Flux<Project> getProjects(Collection<ProjectId> ids);

    Mono<Void> update(Project project);

    Mono<Void> remove(ProjectId projectId);

}
