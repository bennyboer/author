package de.bennyboer.eventsourcing.api.aggregate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregateId {

    String value;

    public static AggregateId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AggregateId must not be null or empty");
        }

        return new AggregateId(value);
    }

    @Override
    public String toString() {
        return String.format("AggregateId(%s)", value);
    }

}
