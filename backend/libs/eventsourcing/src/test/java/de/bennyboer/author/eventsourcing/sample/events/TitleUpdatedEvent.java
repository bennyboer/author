package de.bennyboer.author.eventsourcing.sample.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.sample.commands.UpdateTitleCmd;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TitleUpdatedEvent implements Event {

    private static final EventName NAME = EventName.of("TITLE_UPDATED");

    private static final Version VERSION = Version.zero();

    String title;

    private TitleUpdatedEvent(String title) {
        this.title = title;
    }

    public static TitleUpdatedEvent of(UpdateTitleCmd cmd) {
        return new TitleUpdatedEvent(cmd.getTitle());
    }

    @Override
    @JsonIgnore
    public EventName getEventName() {
        return NAME;
    }

    @Override
    @JsonIgnore
    public Version getVersion() {
        return VERSION;
    }

}
