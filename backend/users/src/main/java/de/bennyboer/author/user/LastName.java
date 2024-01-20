package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@With(AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LastName {

    private static final String ANONYMIZED_VALUE = "ANONYMIZED";

    String value;

    public static LastName of(String value) {
        checkNotNull(value, "Last name must be given");

        return new LastName(value);
    }

    @Override
    public String toString() {
        return String.format("LastName(%s)", value);
    }

    public LastName anonymized() {
        return withValue(ANONYMIZED_VALUE);
    }

}
