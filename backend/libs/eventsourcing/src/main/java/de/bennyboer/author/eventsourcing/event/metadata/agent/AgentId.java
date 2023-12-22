package de.bennyboer.eventsourcing.event.metadata.agent;

import de.bennyboer.author.common.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentId {

    private static final AgentId SYSTEM = new AgentId("SYSTEM");

    private static final AgentId ANONYMOUS = new AgentId("ANONYMOUS");

    String value;

    public static AgentId of(String value) {
        checkNotNull(value, "AgentId must not be null");
        checkArgument(!value.isBlank(), "AgentId must not be blank");

        return new AgentId(value);
    }

    public static AgentId system() {
        return SYSTEM;
    }

    public static AgentId anonymous() {
        return ANONYMOUS;
    }

    public UserId toUserId() {
        return UserId.of(value);
    }

    @Override
    public String toString() {
        return String.format("AgentId(%s)", value);
    }

}
