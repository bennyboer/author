package de.bennyboer.eventsourcing.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventName {

    String value;

    public static EventName of(String name) {
        checkNotNull(name, "Event name must not be null");
        checkArgument(!name.isBlank(), "Event name must not be blank");

        name = name.trim()
                .replaceAll("[^a-zA-Z]", "_")
                .toUpperCase(Locale.ROOT);

        return new EventName(name);
    }

    @Override
    public String toString() {
        return String.format("EventName(%s)", value);
    }

}
