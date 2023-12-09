package de.bennyboer.author.server.shared.websocket.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class EventMessage {

    EventTopicDTO topic;

    String eventName;

    long eventVersion;

    Object payload;

    public static EventMessage of(
            EventTopicDTO topic,
            String eventName,
            long eventVersion,
            Object payload
    ) {
        checkNotNull(topic, "topic must not be null");
        checkNotNull(eventName, "eventName must not be null");
        checkNotNull(payload, "payload must not be null");

        return new EventMessage(topic, eventName, eventVersion, payload);
    }

}
