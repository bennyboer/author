package de.bennyboer.author.server.projects.persistence.lookup;

import de.bennyboer.author.project.ProjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProjectLookupRepo implements ProjectLookupRepo {

    private final Map<ProjectId, LookupProject> projects = new ConcurrentHashMap<>();

    @Override
    public Flux<LookupProject> getProjects(Collection<ProjectId> ids) {
        return Flux.fromIterable(ids)
                .mapNotNull(projects::get);
    }

    @Override
    public Mono<Void> update(LookupProject project) {
        return Mono.fromRunnable(() -> projects.put(project.getId(), project));
    }

    @Override
    public Mono<Void> remove(ProjectId projectId) {
        return Mono.fromRunnable(() -> projects.remove(projectId));
    }

}
