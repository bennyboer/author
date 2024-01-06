package de.bennyboer.author.server.shared.websocket.api;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class UnsubscribedFromPermissionsMessage {

    String aggregateType;

    @Nullable
    String aggregateId;

    @Nullable
    String action;

    public static UnsubscribedFromPermissionsMessage of(
            String aggregateType,
            @Nullable String aggregateId,
            @Nullable String action
    ) {
        checkNotNull(aggregateType, "Aggregate type must be given");

        return new UnsubscribedFromPermissionsMessage(aggregateType, aggregateId, action);
    }

    public Optional<String> getAggregateId() {
        return Optional.ofNullable(aggregateId);
    }

    public Optional<String> getAction() {
        return Optional.ofNullable(action);
    }

}
