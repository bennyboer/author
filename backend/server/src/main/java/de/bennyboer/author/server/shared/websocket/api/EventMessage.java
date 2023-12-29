package de.bennyboer.author.server.shared.websocket.api;

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
        checkNotNull(topic, "topic must be given");
        checkNotNull(eventName, "eventName must be given");
        checkNotNull(payload, "payload must be given");

        return new EventMessage(topic, eventName, eventVersion, payload);
    }

}
