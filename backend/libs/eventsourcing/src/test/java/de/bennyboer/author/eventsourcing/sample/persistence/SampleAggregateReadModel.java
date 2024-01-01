package de.bennyboer.author.eventsourcing.sample.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SampleAggregateReadModel {

    String id;

    String title;

    String description;

    public static SampleAggregateReadModel of(String id, String title, String description) {
        checkNotNull(id, "ID must be given");
        checkNotNull(title, "Title must be given");
        checkNotNull(description, "Description must be given");

        return new SampleAggregateReadModel(id, title, description);
    }

}
