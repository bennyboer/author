package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.EventName;

import java.util.Map;

public interface SubscriptionEventListener {

    void onEvent(
            EventTopic eventTopic,
            EventName eventName,
            Version eventVersion,
            Map<String, Object> payload
    );

}
