package de.bennyboer.author.permissions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceId {

    String value;

    public static ResourceId of(String value) {
        checkNotNull(value, "Resource ID must be given");
        checkArgument(!value.isBlank(), "Resource ID must not be blank");

        return new ResourceId(value);
    }

    @Override
    public String toString() {
        return String.format("ResourceId(%s)", value);
    }

}
