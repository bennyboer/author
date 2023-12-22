package de.bennyboer.author.auth.token;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkArgument;
import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Token {

    String value;

    public static Token of(String value) {
        checkNotNull(value, "Token value must not be null");
        checkArgument(!value.isBlank(), "Token value must not be blank");

        return new Token(value);
    }

}
