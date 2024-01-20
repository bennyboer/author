package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FirstName {

    private static final String ANONYMIZED_VALUE = "ANONYMIZED";

    String value;

    public static FirstName of(String value) {
        checkNotNull(value, "First name must be given");

        return new FirstName(value);
    }

    @Override
    public String toString() {
        return String.format("FirstName(%s)", value);
    }

    public FirstName anonymized() {
        return withValue(ANONYMIZED_VALUE);
    }

}
