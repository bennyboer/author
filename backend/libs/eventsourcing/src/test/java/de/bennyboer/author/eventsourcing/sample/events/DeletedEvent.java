package de.bennyboer.author.eventsourcing.sample.events;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.AbstractEvent;
import de.bennyboer.author.eventsourcing.event.EventName;
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
