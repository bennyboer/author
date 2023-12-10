package de.bennyboer.author.server.shared.websocket.subscriptions;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.EventName;

import java.util.Map;

public interface SubscriptionEventListener {

    void onEvent(
            EventTopic eventTopic,
            EventName eventName,
            Version eventVersion,
            Map<String, Object> payload
    );

}
