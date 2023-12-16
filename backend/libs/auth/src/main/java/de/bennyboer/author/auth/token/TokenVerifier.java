package de.bennyboer.author.auth.token;

import reactor.core.publisher.Mono;

public interface TokenVerifier {

    Mono<TokenContent> verify(Token token);

}
