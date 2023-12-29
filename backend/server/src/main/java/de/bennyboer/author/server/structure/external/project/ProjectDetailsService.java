package de.bennyboer.author.server.structure.external.project;

import reactor.core.publisher.Mono;

public interface ProjectDetailsService {

    Mono<String> getProjectName(String projectId);

}
