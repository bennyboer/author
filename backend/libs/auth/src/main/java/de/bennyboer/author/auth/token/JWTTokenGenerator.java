package de.bennyboer.author.auth.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import de.bennyboer.author.auth.keys.KeyPair;
import de.bennyboer.author.common.UserId;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JWTTokenGenerator implements TokenGenerator {

    private final Algorithm algorithm;

    public JWTTokenGenerator(KeyPair keyPair) {
        algorithm = Algorithm.ECDSA512(keyPair.getPublicKey(), keyPair.getPrivateKey());
    }

    @Override
    public Mono<Token> generate(TokenContent content) {
        return Mono.fromCallable(() -> generateToken(content));
    }

    private Token generateToken(TokenContent content) {
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
        String token = JWT.create()
                .withIssuer("server")
                .withSubject(content.getUserId().map(UserId::getValue).orElse("SYSTEM"))
                .withExpiresAt(expiresAt)
                .sign(algorithm);

        return Token.of(token);
    }

}
