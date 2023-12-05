package de.bennyboer.author.structure.tree.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeId {

    String value;

    public static NodeId of(String value) {
        checkNotNull(value, "NodeId must not be null");
        checkArgument(!value.isBlank(), "NodeId must not be blank");

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
