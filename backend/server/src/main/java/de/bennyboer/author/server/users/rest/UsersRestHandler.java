package de.bennyboer.author.server.users.rest;

import de.bennyboer.author.server.shared.http.Auth;
import de.bennyboer.author.server.users.api.requests.LoginUserRequest;
import de.bennyboer.author.server.users.api.requests.RenameUserRequest;
import de.bennyboer.author.server.users.facade.UsersCommandFacade;
import de.bennyboer.author.server.users.facade.UsersQueryFacade;
import de.bennyboer.author.user.login.UserLockedException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Value
@AllArgsConstructor
public class UsersRestHandler {

    UsersQueryFacade queryFacade;

    UsersCommandFacade commandFacade;

    public void getUser(Context ctx) {
        var userId = ctx.pathParam("userId");

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> queryFacade.getUser(userId, agent))
                .singleOptional()
                .toFuture()
                .thenAccept(tree -> tree.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.NOT_FOUND)
                )));
    }

    public void renameUser(Context ctx) {
        var request = ctx.bodyAsClass(RenameUserRequest.class);
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> commandFacade.rename(userId, version, request.getName(), agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void removeUser(Context ctx) {
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        ctx.future(() -> Auth.toAgent(ctx)
                .flatMap(agent -> commandFacade.remove(userId, version, agent))
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void login(Context ctx) {
        var request = ctx.bodyAsClass(LoginUserRequest.class);

        ctx.future(() -> commandFacade.login(request.getName(), request.getPassword())
                .switchIfEmpty(Mono.defer(() -> {
                    ctx.status(HttpStatus.UNAUTHORIZED);
                    return Mono.empty();
                }))
                .onErrorResume(e -> {
                    if (e instanceof UserLockedException) {
                        ctx.status(HttpStatus.TOO_MANY_REQUESTS);
                    } else {
                        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    log.warn("Login failed", e);
                    return Mono.empty();
                })
                .singleOptional()
                .toFuture()
                .thenAccept(token -> token.ifPresent(ctx::json)));
    }

}
