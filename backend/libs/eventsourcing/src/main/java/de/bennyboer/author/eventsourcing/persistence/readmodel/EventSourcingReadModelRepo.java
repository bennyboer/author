package de.bennyboer.author.eventsourcing.persistence.readmodel;

import reactor.core.publisher.Mono;

public interface EventSourcingReadModelRepo<ID, T> {

    /**
     * Add or update the aggregate in the read model.
     */
    Mono<Void> update(T readModel);

    /**
     * Remove the read model from the read model with the given ID.
     */
    Mono<Void> remove(ID id);

}
