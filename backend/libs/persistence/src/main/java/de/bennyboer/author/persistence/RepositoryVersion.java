package de.bennyboer.author.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositoryVersion implements Comparable<RepositoryVersion> {

    long value;

    public static RepositoryVersion of(long value) {
        checkArgument(value >= 0, "Repository version must be positive");

        return new RepositoryVersion(value);
    }

    public static RepositoryVersion zero() {
        return of(0);
    }

    public RepositoryVersion increase() {
        return of(value + 1);
    }

    @Override
    public int compareTo(RepositoryVersion other) {
        return Long.compare(value, other.value);
    }

    public boolean isEqualTo(RepositoryVersion version) {
        return getValue() == version.getValue();
    }

    public boolean isGreaterThan(RepositoryVersion version) {
        return getValue() > version.getValue();
    }

    public boolean isGreaterThanOrEqualTo(RepositoryVersion version) {
        return isGreaterThan(version) || isEqualTo(version);
    }

}
