package de.bennyboer.author.permissions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceType {

    String name;

    public static ResourceType of(String name) {
        checkNotNull(name, "Resource type name must be given");
        checkArgument(!name.isBlank(), "Resource type name must not be blank");

        return new ResourceType(name);
    }

    @Override
    public String toString() {
        return String.format("ResourceType(%s)", name);
    }

}
