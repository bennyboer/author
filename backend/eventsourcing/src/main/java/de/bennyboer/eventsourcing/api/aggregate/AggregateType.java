package de.bennyboer.eventsourcing.api.aggregate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregateType {

    String value;

    public static AggregateType of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AggregateType must not be null or empty");
        }

        value = value.trim()
                .replaceAll("[^a-zA-Z]", "_")
                .toUpperCase(Locale.ROOT);

        return new AggregateType(value);
    }

    @Override
    public String toString() {
        return String.format("AggregateType(%s)", value);
    }

}
