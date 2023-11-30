package de.bennyboer.eventsourcing.api.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventName {

    String value;

    public static EventName of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event name must not be null or empty");
        }

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
