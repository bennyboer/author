package de.bennyboer.author.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class UserId {

    String value;

    public static UserId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UserId must not be null or blank");
        }

        return new UserId(value);
    }

    @Override
    public String toString() {
        return String.format("UserId(%s)", value);
    }

}
