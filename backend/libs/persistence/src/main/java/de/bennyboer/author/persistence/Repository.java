package de.bennyboer.author.persistence;

import reactor.core.publisher.Mono;

public interface Repository extends AutoCloseable {

    Mono<RepositoryVersion> getVersion();

}
