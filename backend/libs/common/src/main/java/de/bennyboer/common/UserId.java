package de.bennyboer.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

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

    public static UserId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("UserId(%s)", value);
    }

}
