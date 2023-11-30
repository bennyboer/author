package de.bennyboer.eventsourcing.sample.events;

import de.bennyboer.eventsourcing.api.event.AbstractEvent;
import de.bennyboer.eventsourcing.api.event.EventName;
import de.bennyboer.eventsourcing.api.Version;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class DeletedEvent extends AbstractEvent {

    private static final EventName NAME = EventName.of("DELETED");

    private static final Version VERSION = Version.zero();

    private DeletedEvent() {
        super(NAME, VERSION);
    }

    public static DeletedEvent of() {
        return new DeletedEvent();
    }

}
