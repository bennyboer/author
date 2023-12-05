package de.bennyboer.eventsourcing.api.aggregate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregateId {

    String value;

    public static AggregateId of(String value) {
        checkNotNull(value, "AggregateId must not be null");
        checkArgument(!value.isBlank(), "AggregateId must not be blank");

        return new AggregateId(value);
    }

    @Override
    public String toString() {
        return String.format("AggregateId(%s)", value);
    }

}
