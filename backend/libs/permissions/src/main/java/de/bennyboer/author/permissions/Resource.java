package de.bennyboer.author.permissions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Resource {

    ResourceType type;

    ResourceId id;

    public static Resource of(ResourceType type, ResourceId id) {
        checkNotNull(type, "Resource type must be given");
        checkNotNull(id, "Resource ID must be given");

        return new Resource(type, id);
    }

    @Override
    public String toString() {
        return String.format("Resource(%s, %s)", type, id);
    }

}
