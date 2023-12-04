package de.bennyboer.eventsourcing.sample.patches;

import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.api.aggregate.AggregateType;
import de.bennyboer.eventsourcing.api.event.Event;
import de.bennyboer.eventsourcing.api.event.EventName;
import de.bennyboer.eventsourcing.api.patch.Patch;
import de.bennyboer.eventsourcing.sample.SampleAggregate;
import de.bennyboer.eventsourcing.sample.events.CreatedEvent;
import de.bennyboer.eventsourcing.sample.events.CreatedEvent2;

public class CreatedEventPatch1 implements Patch {

    @Override
    public Version fromVersion() {
        return CreatedEvent.VERSION;
    }

    @Override
    public Version toVersion() {
        return CreatedEvent2.VERSION;
    }

    @Override
    public AggregateType aggregateType() {
        return SampleAggregate.TYPE;
    }

    @Override
    public EventName eventName() {
        return CreatedEvent.NAME;
    }

    @Override
    public Event apply(Event event) {
        return CreatedEvent2.from((CreatedEvent) event);
    }

}