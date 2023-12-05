package de.bennyboer.author.server.websocket.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class EventTopicDTO {

    String aggregateType;

    String aggregateId;

    long version;

    public static EventTopicDTO of(String aggregateType, String aggregateId, long version) {
        if (aggregateType == null) {
            throw new IllegalArgumentException("aggregateType must not be null");
        }
        if (aggregateId == null) {
            throw new IllegalArgumentException("aggregateId must not be null");
        }

        return new EventTopicDTO(aggregateType, aggregateId, version);
    }

}
