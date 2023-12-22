package de.bennyboer.author.project;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectName {

    String value;

    public static ProjectName of(String value) {
        checkNotNull(value, "ProjectName must not be null");
        checkArgument(!value.isBlank(), "ProjectName must not be blank");

        return new ProjectName(value);
    }

    @Override
    public String toString() {
        return String.format("ProjectName(%s)", value);
    }

}
