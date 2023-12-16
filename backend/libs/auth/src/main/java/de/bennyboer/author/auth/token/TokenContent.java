package de.bennyboer.author.auth.token;

import de.bennyboer.common.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenContent {

    UserId userId;

    public static TokenContent of(UserId userId) {
        checkNotNull(userId, "User id must not be null");

        return new TokenContent(userId);
    }

}
