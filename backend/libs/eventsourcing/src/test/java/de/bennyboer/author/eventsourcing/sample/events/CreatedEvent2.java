package de.bennyboer.author.eventsourcing.sample.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.eventsourcing.sample.commands.CreateCmd;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

import static de.bennyboer.author.eventsourcing.sample.events.CreatedEvent.NAME;

@Value
@Builder
@Jacksonized
public class CreatedEvent2 implements Event {

    public static final Version VERSION = Version.of(1);

    String title;

    String description;

    /**
     * Note that it does not really make sense to include this field in the command and created event.
     * It is just here to demonstrate a patch.
     */
    Instant deletedAt;

    private CreatedEvent2(String title, String description, Instant deletedAt) {
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
