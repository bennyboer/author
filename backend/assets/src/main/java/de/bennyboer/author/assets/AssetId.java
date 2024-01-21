package de.bennyboer.author.assets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AssetId {

    String value;

    public static AssetId of(String value) {
        checkNotNull(value, "AssetId must be given");
        checkArgument(!value.isBlank(), "AssetId must not be blank");

        return new AssetId(value);
    }

    public static AssetId create() {
        return of(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return String.format("AssetId(%s)", value);
    }

}
