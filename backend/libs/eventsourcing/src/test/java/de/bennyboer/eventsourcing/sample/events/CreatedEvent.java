package de.bennyboer.eventsourcing.sample.events;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.AbstractEvent;
import de.bennyboer.eventsourcing.event.EventName;
import de.bennyboer.eventsourcing.sample.commands.CreateCmd;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class CreatedEvent extends AbstractEvent {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    String title;

    String description;

    private CreatedEvent(String title, String description) {
        super(NAME, VERSION);

        this.title = title;
        this.description = description;
    }

    public static CreatedEvent of(CreateCmd cmd) {
        return new CreatedEvent(cmd.getTitle(), cmd.getDescription());
    }

}
