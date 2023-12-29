package de.bennyboer.author.structure.nodes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeName {

    String value;

    public static NodeName of(String value) {
        checkNotNull(value, "Node name must be given");
        checkArgument(!value.isBlank(), "Node name must not be blank");

        return new NodeName(value);
    }

    @Override
    public String toString() {
        return String.format("NodeName(%s)", value);
    }

}
