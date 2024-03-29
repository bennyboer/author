package de.bennyboer.author.server.users.rest;

import de.bennyboer.author.server.users.api.requests.*;
import de.bennyboer.author.server.users.api.responses.LoginUserResponse;
import de.bennyboer.author.server.users.facade.UsersCommandFacade;
import de.bennyboer.author.server.users.facade.UsersQueryFacade;
import de.bennyboer.author.user.login.UserLockedException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import reactor.core.publisher.Mono;

import static de.bennyboer.author.server.shared.http.ReactiveHandler.handle;

@Slf4j
@Value
@AllArgsConstructor
public class UsersRestHandler {

    UsersQueryFacade queryFacade;

    UsersCommandFacade commandFacade;

    public void getUser(Context ctx) {
        var userId = ctx.pathParam("userId");

        handle(ctx, agent -> queryFacade.getUser(userId, agent).singleOptional(), user -> user.ifPresentOrElse(
                ctx::json,
                () -> ctx.status(HttpStatus.NOT_FOUND)
        ));
    }

    public void updateUserName(Context ctx) {
        var request = ctx.bodyAsClass(UpdateUserNameRequest.class);
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                agent -> commandFacade.updateUserName(userId, version, request.getName(), agent),
                (res) -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void renameFirstName(Context ctx) {
        var request = ctx.bodyAsClass(RenameFirstNameRequest.class);
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                agent -> commandFacade.renameFirstName(userId, version, request.getFirstName(), agent),
                (res) -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void renameLastName(Context ctx) {
        var request = ctx.bodyAsClass(RenameLastNameRequest.class);
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                agent -> commandFacade.renameLastName(userId, version, request.getLastName(), agent),
                (res) -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void updateMail(Context ctx) {
        var request = ctx.bodyAsClass(UpdateMailRequest.class);
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                agent -> commandFacade.updateMail(userId, version, request.getMail(), agent),
                (res) -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void updateImage(Context ctx) {
        var request = ctx.bodyAsClass(UpdateImageRequest.class);
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                agent -> commandFacade.updateImage(userId, version, request.getImageId(), agent),
                (res) -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void confirmMail(Context ctx) {
        var request = ctx.bodyAsClass(ConfirmMailRequest.class);
        var userId = ctx.pathParam("userId");

        handle(
                ctx,
                agent -> commandFacade.confirmMail(userId, request.getMail(), request.getToken(), agent)
                        .onErrorResume(e -> {
                            log.warn("Mail confirmation for user {} failed", userId, e);

                            ctx.status(HttpStatus.UNAUTHORIZED);
                            return Mono.empty();
                        }),
                (res) -> {
                    if (ctx.status() != HttpStatus.UNAUTHORIZED) {
                        ctx.status(HttpStatus.NO_CONTENT);
                    }
                }
        );
    }

    public void changePassword(Context ctx) {
        var request = ctx.bodyAsClass(ChangePasswordRequest.class);
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                agent -> commandFacade.changePassword(userId, version, request.getPassword(), agent),
                (res) -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void removeUser(Context ctx) {
        var userId = ctx.pathParam("userId");
        var version = ctx.queryParamAsClass("version", Long.class).get();

        handle(
                ctx,
                agent -> commandFacade.remove(userId, version, agent),
                (res) -> ctx.status(HttpStatus.NO_CONTENT)
        );
    }

    public void login(Context ctx) {
        var request = ctx.bodyAsClass(LoginUserRequest.class);

        boolean isLoginByMail = request.getMail().isPresent() && request.getName().isEmpty() && request.getMail().map(
                mail -> EmailValidator.getInstance().isValid(mail)).orElse(false);

        String mail = request.getMail().orElse("");
        String username = request.getName().orElse("");
        String password = request.getPassword();

        Mono<LoginUserResponse> login$ = isLoginByMail
                ? commandFacade.loginByMail(mail, password)
                : commandFacade.loginByUserName(username, password);

        handle(
                ctx,
                agent -> login$
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
                        .singleOptional(),
                token -> token.ifPresent(ctx::json)
        );
    }

}
