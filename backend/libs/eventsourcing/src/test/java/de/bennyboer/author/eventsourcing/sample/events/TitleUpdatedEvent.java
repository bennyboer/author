package de.bennyboer.author.eventsourcing.sample.events;

import de.bennyboer.author.eventsourcing.event.AbstractEvent;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.sample.commands.UpdateTitleCmd;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class TitleUpdatedEvent extends AbstractEvent {

    private static final EventName NAME = EventName.of("TITLE_UPDATED");

    private static final Version VERSION = Version.zero();

    String title;

    private TitleUpdatedEvent(String title) {
        super(NAME, VERSION);

        this.title = title;
    }

    public static TitleUpdatedEvent of(UpdateTitleCmd cmd) {
        return new TitleUpdatedEvent(cmd.getTitle());
    }

}
