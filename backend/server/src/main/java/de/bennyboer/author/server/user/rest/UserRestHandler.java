package de.bennyboer.author.server.user.rest;

import de.bennyboer.author.server.user.api.requests.LoginUserRequest;
import de.bennyboer.author.server.user.api.requests.RenameUserRequest;
import de.bennyboer.author.server.user.facade.UserFacade;
import de.bennyboer.common.UserId;
import de.bennyboer.eventsourcing.event.metadata.agent.Agent;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class UserRestHandler {

    UserFacade facade;

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
                .singleOptional()
                .toFuture()
                .thenAccept(token -> token.ifPresentOrElse(
                        ctx::json,
                        () -> ctx.status(HttpStatus.UNAUTHORIZED)
                )));
    }

}
