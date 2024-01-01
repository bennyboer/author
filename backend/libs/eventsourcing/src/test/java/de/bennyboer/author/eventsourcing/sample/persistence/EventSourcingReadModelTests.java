package de.bennyboer.author.eventsourcing.sample.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class EventSourcingReadModelTests {

    private final SampleAggregateReadModelRepo repo = createRepo();

    protected abstract SampleAggregateReadModelRepo createRepo();

    @Test
    void shouldInsertReadModel() {
        // given: a read model to insert
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of("MODEL_ID", "Title", "Description");

        // when: inserting the read model
        repo.update(readModel).block();

        // then: the read model is inserted
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved).isEqualTo(readModel);
    }

    @Test
    void shouldUpdateReadModel() {
        // given: an existing read model
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of("MODEL_ID", "Title", "Description");
        repo.update(readModel).block();

        // when: updating the read model
        SampleAggregateReadModel updatedReadModel = SampleAggregateReadModel.of(
                "MODEL_ID",
                "New Title",
                "New Description"
        );
        repo.update(updatedReadModel).block();

        // then: the read model is updated
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved).isEqualTo(updatedReadModel);
    }

    @Test
    void shouldRemoveReadModel() {
        // given: an existing read model
        SampleAggregateReadModel readModel = SampleAggregateReadModel.of("MODEL_ID", "Title", "Description");
        repo.update(readModel).block();

        // when: removing the read model
        repo.remove("MODEL_ID").block();

        // then: the read model is removed
        SampleAggregateReadModel saved = repo.get("MODEL_ID").block();
        assertThat(saved).isNull();
    }

}
