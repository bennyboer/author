package de.bennyboer.eventsourcing.api;

import lombok.Value;

@Value
public class Version {

    long value;

    public static Version zero() {
        return new Version(0);
    }

    public static Version of(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Version must be greater than 0");
        }

        return new Version(value);
    }

    public Version increment() {
        return new Version(value + 1);
    }

    public boolean isPreviousTo(Version other) {
        return value == other.value + 1;
    }

    public boolean isOlderThan(Version other) {
        return value < other.value;
    }

    @Override
    public String toString() {
        return String.format("Version(%d)", value);
    }

}
