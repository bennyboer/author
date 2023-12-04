package de.bennyboer.author.structure.tree.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeId {

    String value;

    public static TreeId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TreeId must not be null or blank");
        }

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
