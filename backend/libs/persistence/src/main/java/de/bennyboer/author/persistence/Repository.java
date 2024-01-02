package de.bennyboer.author.persistence;

import reactor.core.publisher.Mono;

public interface Repository {

    Mono<RepositoryVersion> getVersion();

}
