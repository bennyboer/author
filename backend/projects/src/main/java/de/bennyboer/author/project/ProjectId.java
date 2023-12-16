package de.bennyboer.author.project;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectId {

    String value;

    public static ProjectId of(String value) {
        checkNotNull(value, "ProjectId must not be null");
        checkArgument(!value.isBlank(), "ProjectId must not be blank");

        return new ProjectId(value);
    }

    public static ProjectId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("ProjectId(%s)", value);
    }

}
