package de.bennyboer.author.server.shared.permissions;

import de.bennyboer.author.permissions.Permission;

public class MissingPermissionException extends RuntimeException {

    public MissingPermissionException(Permission permission) {
        super("Missing permission: " + permission);
    }
}
