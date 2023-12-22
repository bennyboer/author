package de.bennyboer.author.auth.password;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.common.Preconditions.checkNotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodedPassword {

    String value;

    public static EncodedPassword ofRaw(CharSequence rawPassword) {
        checkNotNull(rawPassword, "Raw password must not be null");

        String value = PasswordEncoder.getInstance().encode(rawPassword);
        return new EncodedPassword(value);
    }

    public static EncodedPassword ofEncoded(CharSequence encodedPassword) {
        checkNotNull(encodedPassword, "Encoded password must not be null");

        return new EncodedPassword(encodedPassword.toString());
    }

    public boolean matches(CharSequence rawPassword) {
        checkNotNull(rawPassword, "Raw password must not be null");

        return PasswordEncoder.getInstance().matches(rawPassword, value);
    }

    @Override
    public String toString() {
        return String.format("EncodedPassword(%s)", value);
    }

}
