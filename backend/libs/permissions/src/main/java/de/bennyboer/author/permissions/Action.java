package de.bennyboer.author.permissions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Action {

    String name;

    public static Action of(String name) {
        checkNotNull(name, "Action name must be given");
        checkArgument(!name.isBlank(), "Action name must not be blank");

        return new Action(name);
    }

    @Override
    public String toString() {
        return String.format("Action(%s)", name);
    }

}
