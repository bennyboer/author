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
public class SubscribedMessage {

    String aggregateType;

    String aggregateId;

    @Nullable
    String eventName;

    public static SubscribedMessage of(
            String aggregateType,
            String aggregateId,
            @Nullable String eventName
    ) {
        checkNotNull(aggregateType, "Aggregate type must be given");

        return new SubscribedMessage(aggregateType, aggregateId, eventName);
    }

    public Optional<String> getEventName() {
        return Optional.ofNullable(eventName);
    }

}
