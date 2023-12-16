package de.bennyboer.author.auth.token;

import de.bennyboer.author.auth.keys.KeyPair;

public class TokenGenerators {

    public static TokenGenerator create(KeyPair keyPair) {
        return new JWTTokenGenerator(keyPair);
    }

}
