package de.bennyboer.author.auth.token;

import de.bennyboer.author.auth.keys.KeyPair;

public class TokenVerifiers {

    public static TokenVerifier create(KeyPair keyPair) {
        return new JWTTokenVerifier(keyPair);
    }

}
