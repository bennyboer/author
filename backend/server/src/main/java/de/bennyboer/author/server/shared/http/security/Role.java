package de.bennyboer.author.server.shared.http.security;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    UNAUTHORIZED,
    AUTHORIZED
}
