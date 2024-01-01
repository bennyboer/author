package de.bennyboer.author.eventsourcing.persistence.readmodel;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InMemoryEventSourcingReadModelRepo<ID, T> implements EventSourcingReadModelRepo<ID, T> {

    protected final Map<ID, T> lookup = new ConcurrentHashMap<>();

    protected abstract ID getId(T aggregate);

    public Mono<T> get(ID id) {
        return Mono.justOrEmpty(lookup.get(id));
    }

    @Override
    public Mono<Void> update(T aggregate) {
        return Mono.fromRunnable(() -> {
            lookup.put(getId(aggregate), aggregate);
        });
    }

    @Override
    public Mono<Void> remove(ID id) {
        return Mono.fromRunnable(() -> {
            lookup.remove(id);
        });
    }

}
