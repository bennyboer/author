package de.bennyboer.author.server.users.rest;

import de.bennyboer.author.server.users.api.requests.LoginUserRequest;
import de.bennyboer.author.server.users.api.requests.RenameUserRequest;
import de.bennyboer.author.server.users.facade.UsersFacade;
import de.bennyboer.author.user.login.UserLockedException;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
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

    UsersFacade facade;

    public void getUser(Context ctx) {
        var userId = ctx.pathParam("userId");
        // TODO Get user ID from authentication

        ctx.future(() -> facade.getUser(userId)
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
        var agent = Agent.user(UserId.of("TEST_USER_ID")); // TODO Get agent from authentication

        ctx.future(() -> facade.rename(userId, version, request.getName(), agent)
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void removeUser(Context ctx) {
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();
        var agent = Agent.user(UserId.of("TEST_USER_ID")); // TODO Get agent from authentication

        ctx.future(() -> facade.remove(userId, version, agent)
                .toFuture()
                .thenRun(() -> ctx.status(HttpStatus.NO_CONTENT)));
    }

    public void login(Context ctx) {
        var request = ctx.bodyAsClass(LoginUserRequest.class);

        ctx.future(() -> facade.login(request.getName(), request.getPassword())
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
