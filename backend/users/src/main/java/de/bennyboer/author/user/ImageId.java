package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageId {

    String value;

    public static ImageId of(String value) {
        checkNotNull(value, "Image ID must be given");

        return new ImageId(value);
    }

    @Override
    public String toString() {
        return String.format("ImageId(%s)", value);
    }

}
