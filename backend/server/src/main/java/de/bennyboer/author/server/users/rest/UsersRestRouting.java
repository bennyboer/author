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
            post("/rename", handler::renameUser);
            delete(handler::removeUser);
        });
    }

}
