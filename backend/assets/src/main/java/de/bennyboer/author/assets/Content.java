package de.bennyboer.author.assets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Content {

    String data;

    ContentType type;

    public static Content of(String data, ContentType type) {
        checkNotNull(data, "Data must be given");
        checkNotNull(type, "Type must be given");

        return new Content(data, type);
    }

}
