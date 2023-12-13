package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessToken {

    String value;

    Instant expiresAt;

    public static AccessToken of(String value, Instant expiresAt) {
        checkNotNull(value, "Access token value must not be null");
        checkNotNull(expiresAt, "Access token expiration date must not be null");

        return new AccessToken(value, expiresAt);
    }

    @Override
    public String toString() {
        return String.format("AccessToken(%s, %s)", value, expiresAt);
    }

}
