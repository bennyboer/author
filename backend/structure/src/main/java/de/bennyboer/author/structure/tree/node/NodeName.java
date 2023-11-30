package de.bennyboer.author.structure.tree.node;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeName {

    String value;

    public static NodeName of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("NodeName must not be null or blank");
        }

        return new NodeName(value);
    }

    @Override
    public String toString() {
        return String.format("NodeName(%s)", value);
    }

}
