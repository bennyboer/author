package de.bennyboer.author.eventsourcing.sample.persistence;

import de.bennyboer.author.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Mono;

public interface SampleAggregateReadModelRepo extends EventSourcingReadModelRepo<String, SampleAggregateReadModel> {

    Mono<SampleAggregateReadModel> get(String id);

}
