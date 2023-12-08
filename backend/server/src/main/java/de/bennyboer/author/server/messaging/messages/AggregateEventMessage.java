package de.bennyboer.author.server.messaging.messages;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class AggregateEventMessage {

    String aggregateType;

    String aggregateId;

    long aggregateVersion;

    Instant date;

    String eventName;

    long eventVersion;

    Map<String, Object> payload;

    public static AggregateEventMessage of(
            String aggregateType,
            String aggregateId,
            long aggregateVersion,
            Instant date,
            String eventName,
            long eventVersion,
            Map<String, Object> payload
    ) {
        return new AggregateEventMessage(
                aggregateType,
                aggregateId,
                aggregateVersion,
                date,
                eventName,
                eventVersion,
                payload
        );
    }

}
