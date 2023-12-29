package de.bennyboer.author.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositoryVersion {

    long value;

    public static RepositoryVersion of(long value) {
        checkArgument(value >= 0, "Repository version must be positive");

        return new RepositoryVersion(value);
    }

}
