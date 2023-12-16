package de.bennyboer.author.auth.token;

import reactor.core.publisher.Mono;

public interface TokenGenerator {

    Mono<Token> generate(TokenContent content);

}
