package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessToken {

    String value;

    public static AccessToken of(String value) {
        checkNotNull(value, "Access token value must be given");

        return new AccessToken(value);
    }

    @Override
    public String toString() {
        return String.format("AccessToken(%s)", value);
    }

}
