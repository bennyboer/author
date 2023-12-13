package de.bennyboer.author.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.common.Preconditions.checkArgument;
import static de.bennyboer.common.Preconditions.checkNotNull;

/**
 * This may represent a password in its raw or encoded form.
 * When entering the password into the system this is usually a raw password.
 * When the password is stored in the system it is usually an encoded password.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Password {

    CharSequence value;

    public static Password of(CharSequence value) {
        checkNotNull(value, "Password value must not be null");
        checkArgument(value.length() >= 8, "Password must be at least 8 characters long");

        return new Password(value);
    }

    @Override
    public String toString() {
        return String.format("Password(%s)", "********");
    }

}
