package de.bennyboer.author.eventsourcing.aggregate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AggregateType {

    String value;

    public static AggregateType of(String value) {
        checkNotNull(value, "AggregateType must be given");
        checkArgument(!value.isBlank(), "AggregateType must not be blank");

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
