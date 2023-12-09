package de.bennyboer.author.server.shared.websocket.api;

import de.bennyboer.author.server.shared.websocket.subscriptions.SubscriptionTarget;
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
public class SubscribeMessage {

    SubscriptionTarget target;

    public static SubscribeMessage of(SubscriptionTarget target) {
        checkNotNull(target, "target must not be null");

        return new SubscribeMessage(target);
    }

}
