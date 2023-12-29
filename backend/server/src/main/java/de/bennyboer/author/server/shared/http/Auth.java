package de.bennyboer.author.server.shared.http;

import de.bennyboer.author.auth.token.Token;
import de.bennyboer.author.auth.token.TokenVerifier;
import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import io.javalin.http.Context;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
public class Auth {

    @Getter
    private static Token systemToken;
    private static TokenVerifier tokenVerifier;

    public static void init(TokenVerifier tokenVerifier, Token systemToken) {
        Auth.tokenVerifier = tokenVerifier;
        Auth.systemToken = systemToken;
    }

    public static Mono<Agent> toAgent(Context ctx) {
        // TODO Add a backend token that is to be mapped to the system agent
        return extractToken(ctx)
                .flatMap(Auth::toAgent)
                .defaultIfEmpty(Agent.anonymous());
    }

    public static Mono<Agent> toAgent(Token token) {
        return Mono.just(token)
                .flatMap(tokenVerifier::verify)
                .map(content -> content.getUserId()
                        .map(Agent::user)
                        .orElse(Agent.system()))
                .onErrorResume(throwable -> {
                    log.warn("Could not verify access token", throwable);

                    return Mono.just(Agent.anonymous());
                })
                .defaultIfEmpty(Agent.anonymous());
    }

    private static Mono<Token> extractToken(Context ctx) {
        return Mono.justOrEmpty(Optional.ofNullable(ctx.header("Authorization")))
                .filter(token -> token.startsWith("Bearer "))
                .map(token -> token.substring(7).trim())
                .map(Token::of);
    }

}
