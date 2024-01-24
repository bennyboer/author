package de.bennyboer.author.user.image;

import de.bennyboer.author.eventsourcing.command.Command;
import de.bennyboer.author.user.ImageId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateImageCmd implements Command {

    ImageId imageId;

    public static UpdateImageCmd of(ImageId imageId) {
        checkNotNull(imageId, "Image ID must be given");

        return new UpdateImageCmd(imageId);
    }

}
