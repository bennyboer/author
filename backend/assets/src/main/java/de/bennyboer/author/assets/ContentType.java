package de.bennyboer.author.assets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Locale;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

/**
 * Represents the type of content.
 * This should be mostly compatible with MIME types.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentType {

    String type;

    String subType;

    public static ContentType fromMimeType(String mimeType) {
        checkNotNull(mimeType, "MIME type must not be null");

        String[] parts = mimeType.split("/");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid MIME type: %s".formatted(mimeType));
        }

        String type = parts[0].toLowerCase(Locale.ROOT);
        String subType = parts[1].toLowerCase(Locale.ROOT);

        return new ContentType(type, subType);
    }

    public String asMimeType() {
        return "%s/%s".formatted(type, subType);
    }

    @Override
    public String toString() {
        return "ContentType(%s)".formatted(asMimeType());
    }

}
