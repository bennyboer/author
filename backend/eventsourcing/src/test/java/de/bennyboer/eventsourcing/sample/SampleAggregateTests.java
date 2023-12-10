package de.bennyboer.eventsourcing.sample;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.aggregate.AggregateId;
import de.bennyboer.eventsourcing.event.EventWithMetadata;
import de.bennyboer.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.eventsourcing.event.metadata.agent.AgentType;
import de.bennyboer.eventsourcing.persistence.EventSourcingRepo;
import de.bennyboer.eventsourcing.persistence.InMemoryEventSourcingRepo;
import de.bennyboer.eventsourcing.sample.commands.CreateCmd;
import de.bennyboer.eventsourcing.sample.events.CreatedEvent;
import de.bennyboer.eventsourcing.sample.events.CreatedEvent2;
import de.bennyboer.eventsourcing.testing.TestEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SampleAggregateTests {

    private final EventSourcingRepo repo = new InMemoryEventSourcingRepo();

    private final TestEventPublisher eventPublisher = new TestEventPublisher();

    private final SampleAggregateService eventSourcingService = new SampleAggregateService(
            repo,
            eventPublisher
    );

    @Test
    void shouldCreate() {
        var id = "SAMPLE_ID";

        // when: the aggregate is created
        var version = eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();

        // then: the retrieved aggregate has the correct title and description
        var aggregate = eventSourcingService.get(id).block();
        assertEquals(id, aggregate.getId());
        assertEquals("Test title", aggregate.getTitle());
        assertEquals("Test description", aggregate.getDescription());
        assertFalse(aggregate.isDeleted());

        // and: the version is 0
        assertEquals(Version.zero(), version);

        // and: an event has been published
        var events = eventPublisher.findEventsByName(CreatedEvent.NAME);
        assertEquals(1, events.size());
        EventWithMetadata eventWithMetadata = events.getFirst();

        var metadata = eventWithMetadata.getMetadata();
        assertEquals(id, metadata.getAggregateId().getValue());
        assertEquals(SampleAggregate.TYPE, metadata.getAggregateType());
        assertEquals(Version.zero(), metadata.getAggregateVersion());
        assertEquals(AgentType.USER, metadata.getAgent().getType());
        assertEquals("USER_ID", metadata.getAgent().getId().getValue());

        var event = eventWithMetadata.getEvent();
        assertInstanceOf(CreatedEvent2.class, event);
        var createdEvent = (CreatedEvent2) event;
        assertEquals("Test title", createdEvent.getTitle());
        assertEquals("Test description", createdEvent.getDescription());
    }

    @Test
    void shouldUpdateTitle() {
        var id = "SAMPLE_ID";

        // given: an aggregate with a title
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();

        // when: the title is updated
        var version = eventSourcingService.updateTitle(id, initialVersion, "New title", "USER_ID").block();

        // then: the retrieved aggregate has the correct title
        var aggregate = eventSourcingService.get(id).block();
        assertEquals("New title", aggregate.getTitle());
        assertEquals("Test description", aggregate.getDescription());
        assertFalse(aggregate.isDeleted());

        // and: the version is 1
        assertEquals(Version.of(1), version);
    }

    @Test
    void shouldUpdateDescription() {
        var id = "SAMPLE_ID";

        // given: an aggregate with a description
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();

        // when: the description is updated
        var version = eventSourcingService.updateDescription(id, initialVersion, "New description", "USER_ID").block();

        // then: the retrieved aggregate has the correct description
        var aggregate = eventSourcingService.get(id).block();
        assertEquals("Test title", aggregate.getTitle());
        assertEquals("New description", aggregate.getDescription());
        assertFalse(aggregate.isDeleted());

        // and: the version is 1
        assertEquals(Version.of(1), version);
    }

    @Test
    void shouldDelete() {
        var id = "SAMPLE_ID";

        // given: an aggregate
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();

        // when: the aggregate is deleted
        var version = eventSourcingService.delete(id, initialVersion, "USER_ID").block();

        // then: the retrieved aggregate is deleted
        var aggregate = eventSourcingService.get(id).block();
        assertNull(aggregate);

        // and: the version is 1
        assertEquals(Version.of(1), version);
    }

    @Test
    void shouldNotBeAbleToDispatchCommandsAfterDelete() {
        var id = "SAMPLE_ID";

        // given: a deleted aggregate
        var initialVersion = eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();
        var version = eventSourcingService.delete(id, initialVersion, "USER_ID").block();

        // when: another command is dispatched
        Executable executable = () -> eventSourcingService.updateTitle(id, version, "New title", "USER_ID").block();

        // then: an exception is thrown
        var exception = assertThrows(IllegalStateException.class, executable);
        assertEquals("Cannot apply command to deleted aggregate", exception.getMessage());
    }

    @Test
    void shouldBeAbleToRetrieveOldVersions() {
        var id = "SAMPLE_ID";

        // given: an aggregate that underwent multiple changes
        var version = eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();
        version = eventSourcingService.updateTitle(id, version, "New title", "USER_ID").block();
        version = eventSourcingService.updateDescription(id, version, "New description", "USER_ID").block();
        version = eventSourcingService.updateTitle(id, version, "Newer title", "USER_ID").block();
        version = eventSourcingService.delete(id, version, "USER_ID").block();

        // when: the aggregate is retrieved in an old version
        var aggregate = eventSourcingService.get(id, Version.of(2)).block();

        // then: the aggregate has the correct title and description
        assertEquals("New title", aggregate.getTitle());
        assertEquals("New description", aggregate.getDescription());
        assertFalse(aggregate.isDeleted());
    }

    @Test
    void shouldBeAbleToManageMultipleAggregates() {
        var id1 = "SAMPLE_ID_1";
        var id2 = "SAMPLE_ID_2";

        // given: two aggregates
        var version1 = eventSourcingService.create(id1, "Test title 1", "Test description 1", "USER_ID").block();
        var version2 = eventSourcingService.create(id2, "Test title 2", "Test description 2", "USER_ID").block();
        version2 = eventSourcingService.updateTitle(id2, version2, "New title 2", "USER_ID").block();

        // when: the aggregates are retrieved
        var aggregate1 = eventSourcingService.get(id1).block();
        var aggregate2 = eventSourcingService.get(id2).block();

        // then: the aggregates have the correct titles and descriptions
        assertEquals("Test title 1", aggregate1.getTitle());
        assertEquals("Test description 1", aggregate1.getDescription());
        assertFalse(aggregate1.isDeleted());

        assertEquals("New title 2", aggregate2.getTitle());
        assertEquals("Test description 2", aggregate2.getDescription());
        assertFalse(aggregate2.isDeleted());

        // and: the versions are correct
        assertEquals(Version.zero(), version1);
        assertEquals(Version.of(1), version2);
    }

    @Test
    void shouldSnapshotEvery100Events() {
        var id = "SAMPLE_ID";

        // given: an aggregate
        var version = eventSourcingService.create(id, "Test title", "Test description", "USER_ID").block();

        // when: the aggregate undergoes 300 changes
        for (int i = 0; i < 300; i++) {
            version = eventSourcingService.updateTitle(id, version, "New title " + i, "USER_ID").block();
        }

        // then: each 100th event is a snapshot
        var events = repo.findEventsByAggregateIdAndType(AggregateId.of(id), SampleAggregate.TYPE, Version.zero())
                .collectList()
                .block();
        assertEquals(304, events.size());
        for (int i = 0; i < events.size(); i++) {
            var event = events.get(i);

            if (Set.of(100, 200, 300).contains(i)) {
                assertTrue(event.getMetadata().isSnapshot());
            } else {
                assertFalse(event.getMetadata().isSnapshot());
            }
        }
    }

    @Test
    void shouldPatchOldEventVersionsToTheLatestVersion() {
        var id = "SAMPLE_ID";

        // given: an aggregate that underwent an old create change
        var testDeletedAt = Instant.parse("2021-01-01T00:00:00.000Z");
        var oldEvent = CreatedEvent.of(CreateCmd.of("Test title", "Test description", testDeletedAt));
        var oldEventWithMetadata = EventWithMetadata.of(oldEvent, EventMetadata.of(
                AggregateId.of(id),
                SampleAggregate.TYPE,
                Version.zero(),
                Agent.of(AgentType.USER, AgentId.of("USER_ID")),
                Instant.now(),
                false
        ));
        repo.insert(oldEventWithMetadata).block();

        // when: the aggregate is retrieved
        var aggregate = eventSourcingService.get(id).block();

        // then: the aggregate does not have the deletedAt field set since only the new created event has it
        assertFalse(aggregate.isDeleted());
    }

}
