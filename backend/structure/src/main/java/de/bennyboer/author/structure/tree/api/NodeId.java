package de.bennyboer.author.structure.tree.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeId {

    String value;

    public static NodeId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("NodeId must not be null or blank");
        }

        return new NodeId(value);
    }

    public static NodeId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("NodeId(%s)", value);
    }

}
