package de.bennyboer.eventsourcing.api.event.metadata.agent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

/**
 * An agent is a user or a system that performs commands on an aggregate.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Agent {

    AgentType type;

    AgentId id;

    public static Agent of(AgentType type, AgentId id) {
        checkNotNull(type, "AgentType must not be null");
        checkNotNull(id, "AgentId must not be null");

        return new Agent(type, id);
    }

    @Override
    public String toString() {
        return String.format("Agent(%s, %s)", type, id);
    }

}
