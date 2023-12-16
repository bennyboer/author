package de.bennyboer.author.auth.keys;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeyReaderTests {

    @Test
    void shouldReadKeyPair() {
        // given: a path to the key pair
        String path = "/keys/key_pair.pem";

        // when: reading the key pair
        KeyPair keyPair = KeyReader.readKeyPair(path).block();

        // then: the key pair should not be null
        assertNotNull(keyPair);

        // and: the public key should not be null
        assertNotNull(keyPair.getPublicKey());

        // and: the private key should not be null
        assertNotNull(keyPair.getPrivateKey());
    }

}
