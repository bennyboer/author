package de.bennyboer.author.assets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

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

    public static ContentType of(String type, String subType) {
        checkNotNull(type, "Type must not be null");
        checkNotNull(subType, "Sub type must not be null");

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
