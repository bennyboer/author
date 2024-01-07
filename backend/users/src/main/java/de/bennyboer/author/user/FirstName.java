package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FirstName {

    String value;

    public static FirstName of(String value) {
        checkNotNull(value, "First name must be given");

        return new FirstName(value);
    }

    @Override
    public String toString() {
        return String.format("FirstName(%s)", value);
    }

}
