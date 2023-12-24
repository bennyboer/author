package de.bennyboer.author.server.shared.messaging.messages;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class AggregateEventMessage {

    String aggregateType;

    String aggregateId;

    long aggregateVersion;

    @Nullable
    String userId;

    Instant date;

    String eventName;

    long eventVersion;

    Map<String, Object> payload;

    public static AggregateEventMessage of(
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            @Nullable String userId,
            Instant date,
            String eventName,
            long eventVersion,
            Map<String, Object> payload
    ) {
        return new AggregateEventMessage(
                aggregateType,
                aggregateId,
                aggregateVersion,
                userId,
                date,
                eventName,
                eventVersion,
                payload
        );
    }

    public Optional<String> getUserId() {
        return Optional.ofNullable(userId);
    }

}
