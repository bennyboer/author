package de.bennyboer.author.eventsourcing.testing;

import de.bennyboer.author.eventsourcing.EventPublisher;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.event.EventWithMetadata;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class TestEventPublisher implements EventPublisher {

    private final List<EventWithMetadata> publishedEvents = new ArrayList<>();

    public void clear() {
        publishedEvents.clear();
    }

    public List<EventWithMetadata> findEventsByName(EventName eventName) {
        return publishedEvents.stream()
                .filter(eventWithMetadata -> eventWithMetadata.getEvent().getEventName().equals(eventName))
                .toList();
    }

    @Override
    public Mono<Void> publish(EventWithMetadata event) {
        return Mono.fromRunnable(() -> publishedEvents.add(event));
    }

}
