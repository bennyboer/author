package de.bennyboer.eventsourcing.event.metadata.agent;

import de.bennyboer.common.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

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

    public static Agent user(UserId userId) {
        return of(AgentType.USER, AgentId.of(userId.getValue()));
    }

    public static Agent system() {
        return of(AgentType.SYSTEM, AgentId.system());
    }

    public static Agent anonymous() {
        return of(AgentType.ANONYMOUS, AgentId.anonymous());
    }

    public boolean isSystem() {
        return type == AgentType.SYSTEM;
    }

    public boolean isAnonymous() {
        return type == AgentType.ANONYMOUS;
    }

    public Optional<UserId> getUserId() {
        if (type == AgentType.USER) {
            return Optional.of(id).map(AgentId::toUserId);
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return String.format("Agent(%s, %s)", type, id);
    }

}
