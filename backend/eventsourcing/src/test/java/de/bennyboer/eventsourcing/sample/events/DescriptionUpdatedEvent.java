package de.bennyboer.eventsourcing.sample.events;

import de.bennyboer.eventsourcing.api.event.AbstractEvent;
import de.bennyboer.eventsourcing.api.event.EventName;
import de.bennyboer.eventsourcing.api.Version;
import de.bennyboer.eventsourcing.sample.commands.UpdateDescriptionCmd;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class DescriptionUpdatedEvent extends AbstractEvent {

    private static final EventName NAME = EventName.of("DESCRIPTION_UPDATED");

    private static final Version VERSION = Version.zero();

    String description;

    private DescriptionUpdatedEvent(String description) {
        super(NAME, VERSION);

        this.description = description;
    }

    public static DescriptionUpdatedEvent of(UpdateDescriptionCmd cmd) {
        return new DescriptionUpdatedEvent(cmd.getDescription());
    }

}
