package de.bennyboer.eventsourcing.sample;

import de.bennyboer.eventsourcing.api.persistence.InMemoryEventSourcingRepo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SampleAggregateTests {

    private final SampleAggregateEventSourcingService eventSourcingService =
            new SampleAggregateEventSourcingService(new InMemoryEventSourcingRepo());

    @Test
    void shouldCreate() {
        var id = "SAMPLE_ID_1";

        // when: the aggregate is created
        eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();

        // then: the retrieved aggregate has the correct title and description
        var aggregate = eventSourcingService.get(id).block();
        assertEquals(id, aggregate.getId());
        assertEquals("Test title", aggregate.getTitle());
        assertEquals("Test description", aggregate.getDescription());
        assertFalse(aggregate.isDeleted());
    }

    // TODO test updating title

    // TODO test updating description

    // TODO test deleting

    // TODO test getting different versions

    // TODO test snapshotting

    // TODO implement and test patches

}
