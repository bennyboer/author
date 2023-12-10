package de.bennyboer.eventsourcing.sample.events;

import de.bennyboer.eventsourcing.Version;
import de.bennyboer.eventsourcing.event.AbstractEvent;
import de.bennyboer.eventsourcing.sample.commands.CreateCmd;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Instant;

@Value
@EqualsAndHashCode(callSuper = true)
public class CreatedEvent2 extends AbstractEvent {

    public static final Version VERSION = Version.of(1);

    String title;

    String description;

    /**
     * Note that it does not really make sense to include this field in the command and created event.
     * It is just here to demonstrate a patch.
     */
    Instant deletedAt;

    private CreatedEvent2(String title, String description, Instant deletedAt) {
        super(CreatedEvent.NAME, VERSION);

        this.title = title;
        this.description = description;
        this.deletedAt = deletedAt;
    }

    public static CreatedEvent2 of(CreateCmd cmd) {
        return new CreatedEvent2(cmd.getTitle(), cmd.getDescription(), cmd.getDeletedAt());
    }

    public static CreatedEvent2 from(CreatedEvent e) {
        return new CreatedEvent2(e.getTitle(), e.getDescription(), null);
    }

}
