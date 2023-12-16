package de.bennyboer.author.structure.tree.nodes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeName {

    String value;

    public static NodeName of(String value) {
        checkNotNull(value, "NodeName must not be null");
        checkArgument(!value.isBlank(), "NodeName must not be blank");

        return new NodeName(value);
    }

    @Override
    public String toString() {
        return String.format("NodeName(%s)", value);
    }

}
