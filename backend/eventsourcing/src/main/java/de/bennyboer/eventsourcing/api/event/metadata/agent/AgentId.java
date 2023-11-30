package de.bennyboer.eventsourcing.api.event.metadata.agent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentId {

    String value;

    public static AgentId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value must not be null or empty");
        }

        return new AgentId(value);
    }

    @Override
    public String toString() {
        return String.format("AgentId(%s)", value);
    }

}
