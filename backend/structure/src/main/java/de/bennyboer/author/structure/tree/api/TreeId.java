package de.bennyboer.author.structure.tree.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeId {

    String value;

    public static TreeId of(String value) {
        checkNotNull(value, "TreeId must not be null");
        checkArgument(!value.isBlank(), "TreeId must not be blank");

        return new TreeId(value);
    }

    public static TreeId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("TreeId(%s)", value);
    }

}
