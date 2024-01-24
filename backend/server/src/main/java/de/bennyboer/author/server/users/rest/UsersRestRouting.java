package de.bennyboer.author.server.users.rest;

import io.javalin.apibuilder.EndpointGroup;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.author.server.shared.http.security.Role.UNAUTHORIZED;
import static io.javalin.apibuilder.ApiBuilder.*;

@Value
@AllArgsConstructor
public class UsersRestRouting implements EndpointGroup {

    UsersRestHandler handler;

    @Override
    public void addEndpoints() {
        post("/login", handler::login, UNAUTHORIZED);
        path("/{userId}", () -> {
            get(handler::getUser);
            post("/username", handler::updateUserName);
            path("/mail", () -> {
                post(handler::updateMail);
                post("/confirm", handler::confirmMail, UNAUTHORIZED);
            });
            post("/password", handler::changePassword);
            post("/rename/firstname", handler::renameFirstName);
            post("/rename/lastname", handler::renameLastName);
            post("/image", handler::updateImage);
            delete(handler::removeUser);
        });
    }

}
