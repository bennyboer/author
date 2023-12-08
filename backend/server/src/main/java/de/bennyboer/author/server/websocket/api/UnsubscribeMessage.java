package de.bennyboer.author.server.websocket.api;

import de.bennyboer.author.server.websocket.subscriptions.SubscriptionTarget;
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
public class UnsubscribeMessage {

    SubscriptionTarget target;

    public static UnsubscribeMessage of(SubscriptionTarget target) {
        checkNotNull(target, "target must not be null");
        
        return new UnsubscribeMessage(target);
    }

}
