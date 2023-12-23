package de.bennyboer.author.eventsourcing.aggregate;

import de.bennyboer.author.eventsourcing.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregateIdAndVersion<ID> {

    ID id;

    Version version;

    public static <ID> AggregateIdAndVersion<ID> of(ID id, Version version) {
        checkNotNull(id, "Id must not be null");
        checkNotNull(version, "Version must not be null");

        return new AggregateIdAndVersion<>(id, version);
    }

}
