package de.bennyboer.author.server.shared.permissions;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class AggregatePermissionEventMessage {

    AggregatePermissionEventType eventType;

    String userId;

    String aggregateType;

    @Nullable
    String aggregateId;

    String action;

    public static AggregatePermissionEventMessage of(
            AggregatePermissionEventType eventType,
            String userId,
            String aggregateType,
            String aggregateId,
            String action
    ) {
        return new AggregatePermissionEventMessage(
                eventType,
                userId,
                aggregateType,
                aggregateId,
                action
        );
    }

    public Optional<String> getAggregateId() {
        return Optional.ofNullable(aggregateId);
    }

}
