package de.bennyboer.author.eventsourcing.sample.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DeletedEvent implements Event {

    private static final EventName NAME = EventName.of("DELETED");

    private static final Version VERSION = Version.zero();

    public static DeletedEvent of() {
        return new DeletedEvent();
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
