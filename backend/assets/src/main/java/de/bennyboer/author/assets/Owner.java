package de.bennyboer.author.assets;

import de.bennyboer.author.common.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Owner {

    UserId userId;

    public static Owner of(UserId userId) {
        checkNotNull(userId, "Owner User ID must be given");

        return new Owner(userId);
    }

    @Override
    public String toString() {
        return "Owner(%s)".formatted(userId);
    }

}
