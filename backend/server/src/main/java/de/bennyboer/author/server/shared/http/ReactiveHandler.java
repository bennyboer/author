package de.bennyboer.author.server.shared.http;

import de.bennyboer.author.eventsourcing.event.metadata.agent.Agent;
import io.javalin.http.Context;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

public class ReactiveHandler {

    public static <T> void handle(Context ctx, Function<Agent, Mono<T>> function, Consumer<T> consumer) {
        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(function)
                .toFuture()
                .thenAccept(consumer));
    }

}
