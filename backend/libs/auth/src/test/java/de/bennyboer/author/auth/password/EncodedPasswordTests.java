package de.bennyboer.author.auth.password;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncodedPasswordTests {

    @Test
    void shouldEncodeAndVerifyPassword() {
        // given: a raw password to encode
        String rawPassword = "MySecretPassword";

        // when: encoding the raw password
        EncodedPassword encodedPassword = EncodedPassword.ofRaw(rawPassword);

        // then: the encoded password should match the raw password
        assertTrue(encodedPassword.matches(rawPassword));

        // and: the encoded password should not match another raw password
        assertFalse(encodedPassword.matches("AnotherPassword"));
    }

}
