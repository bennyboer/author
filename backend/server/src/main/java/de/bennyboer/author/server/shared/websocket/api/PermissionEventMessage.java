package de.bennyboer.author.server.shared.websocket.api;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class PermissionEventMessage {

    EventType type;

    String userId;

    String aggregateType;

    @Nullable
    String aggregateId;

    String action;

    public static PermissionEventMessage of(
            EventType type,
            String userId,
            String aggregateType,
            @Nullable String aggregateId,
            String action
    ) {
        checkNotNull(type, "Event type must be given");
        checkNotNull(userId, "User ID must be given");
        checkNotNull(aggregateType, "Aggregate type must be given");
        checkNotNull(action, "Action must be given");

        return new PermissionEventMessage(type, userId, aggregateType, aggregateId, action);
    }

    public enum EventType {
        ADDED,
        REMOVED
    }

}
