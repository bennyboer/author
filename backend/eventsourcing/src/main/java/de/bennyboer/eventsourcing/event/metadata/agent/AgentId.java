package de.bennyboer.eventsourcing.event.metadata.agent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentId {

    String value;

    public static AgentId of(String value) {
        checkNotNull(value, "AgentId must not be null");
        checkArgument(!value.isBlank(), "AgentId must not be blank");

        return new AgentId(value);
    }

    @Override
    public String toString() {
        return String.format("AgentId(%s)", value);
    }

}
