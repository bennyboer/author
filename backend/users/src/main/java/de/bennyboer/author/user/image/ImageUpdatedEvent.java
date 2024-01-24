package de.bennyboer.author.user.image;

import de.bennyboer.author.eventsourcing.Version;
import de.bennyboer.author.eventsourcing.event.Event;
import de.bennyboer.author.eventsourcing.event.EventName;
import de.bennyboer.author.user.ImageId;
import de.bennyboer.author.user.UserEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageUpdatedEvent implements Event {

    private static final Version VERSION = Version.zero();

    ImageId imageId;

    public static ImageUpdatedEvent of(ImageId imageId) {
        checkNotNull(imageId, "Image ID must be given");

        return new ImageUpdatedEvent(imageId);
    }

    @Override
    public EventName getEventName() {
        return UserEvent.IMAGE_UPDATED.getName();
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
