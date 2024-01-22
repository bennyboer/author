package de.bennyboer.author.assets.create;

import de.bennyboer.author.assets.AssetEvent;
import de.bennyboer.author.assets.Content;
import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatedEvent implements Event {

    private static final Version VERSION = Version.zero();

    Content content;

    public static CreatedEvent of(Content content) {
        checkNotNull(content, "Content must be given");

        return new CreatedEvent(content);
    }

    @Override
    public EventName getEventName() {
        return AssetEvent.CREATED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
