package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LastName {

    String value;

    public static LastName of(String value) {
        checkNotNull(value, "Last name must be given");

        return new LastName(value);
    }

    @Override
    public String toString() {
        return String.format("LastName(%s)", value);
    }

}
