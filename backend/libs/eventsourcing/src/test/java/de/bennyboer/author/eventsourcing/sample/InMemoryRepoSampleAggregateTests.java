package de.bennyboer.author.eventsourcing.sample;

import de.bennyboer.author.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.author.eventsourcing.persistence.InMemoryEventSourcingRepo;

public class InMemoryRepoSampleAggregateTests extends SampleAggregateTests {

    @Override
    protected EventSourcingRepo createRepo() {
        return new InMemoryEventSourcingRepo();
    }
    
}
