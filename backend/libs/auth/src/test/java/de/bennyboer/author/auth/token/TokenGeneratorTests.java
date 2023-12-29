package de.bennyboer.author.auth.token;

import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.auth.keys.KeyPairs;
import de.bennyboer.author.common.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TokenGeneratorTests {

    @Test
    void shouldGenerateToken() {
        // given: a token generator and verifier
        KeyPair keyPair = KeyPairs.read("/keys/key_pair.pem");
        TokenGenerator tokenGenerator = TokenGenerators.create(keyPair);
        TokenVerifier tokenVerifier = TokenVerifiers.create(keyPair);

        // when: generating a token
        Token token = tokenGenerator.generate(TokenContent.user(UserId.of("TEST_USER_ID"))).block();

        // then: the token should not be null
        assertNotNull(token);

        // and: the token should contain the user ID
        TokenContent content = tokenVerifier.verify(token).block();
        assertEquals(UserId.of("TEST_USER_ID"), content.getUserId().orElseThrow());
    }

}
