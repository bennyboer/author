package de.bennyboer.author.structure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StructureId {

    String value;

    public static StructureId of(String value) {
        checkNotNull(value, "Structure ID must be given");
        checkArgument(!value.isBlank(), "Structure ID must not be blank");

        return new StructureId(value);
    }

    public static StructureId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("StructureId(%s)", value);
    }

}
