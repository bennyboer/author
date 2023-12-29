package de.bennyboer.author.auth.token;

import de.bennyboer.author.common.UserId;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenContent {

    boolean isSystem;

    @Nullable
    UserId userId;

    public static TokenContent system() {
        return new TokenContent(true, null);
    }

    public static TokenContent user(UserId userId) {
        checkNotNull(userId, "User id must be given");

        return new TokenContent(false, userId);
    }

    public Optional<UserId> getUserId() {
        return Optional.ofNullable(userId);
    }

}
