package de.bennyboer.author.eventsourcing.sample.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.sample.commands.UpdateDescriptionCmd;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DescriptionUpdatedEvent implements Event {

    private static final EventName NAME = EventName.of("DESCRIPTION_UPDATED");

    private static final Version VERSION = Version.zero();

    String description;

    private DescriptionUpdatedEvent(String description) {
        this.description = description;
    }

    public static DescriptionUpdatedEvent of(UpdateDescriptionCmd cmd) {
        return new DescriptionUpdatedEvent(cmd.getDescription());
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
