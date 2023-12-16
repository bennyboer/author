package de.bennyboer.author.auth.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.common.UserId;
import reactor.core.publisher.Mono;

public class JWTTokenVerifier implements TokenVerifier {

    private final JWTVerifier verifier;

    public JWTTokenVerifier(KeyPair keyPair) {
        Algorithm algorithm = Algorithm.ECDSA512(keyPair.getPublicKey(), keyPair.getPrivateKey());
        verifier = JWT.require(algorithm)
                .withIssuer("server")
                .build();
    }

    @Override
    public Mono<TokenContent> verify(Token token) {
        return Mono.fromCallable(() -> verifier.verify(token.getValue()))
                .map(this::extractTokenContent);
    }

    private TokenContent extractTokenContent(DecodedJWT jwt) {
        UserId userId = UserId.of(jwt.getSubject());

        return TokenContent.of(userId);
    }

}
