package de.bennyboer.author.server.shared.messaging;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageListenerId {

    String value;

    public static MessageListenerId of(String value) {
        checkNotNull(value, "Message listener ID must be given");
        checkArgument(!value.isBlank(), "Message listener ID must not be empty");
        
        return new MessageListenerId(value);
    }

    public static MessageListenerId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("MessageListenerId(%s)", value);
    }

}
